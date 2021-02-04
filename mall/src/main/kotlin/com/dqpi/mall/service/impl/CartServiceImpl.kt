package com.dqpi.mall.service.impl

import com.dqpi.mall.consts.ProductEnum.ON_SALE
import com.dqpi.mall.consts.ResponseEnum.*
import com.dqpi.mall.entity.Cart
import com.dqpi.mall.exceptions.BusinessException
import com.dqpi.mall.repository.ProductRepository
import com.dqpi.mall.service.CartService
import com.dqpi.mall.utils.toCartProductVo
import com.dqpi.mall.utils.toResponseVo
import com.dqpi.mall.vo.CartAddForm
import com.dqpi.mall.vo.CartUpdateForm
import com.dqpi.mall.vo.CartVo
import com.dqpi.mall.vo.ResponseVo
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.entriesAsFlow
import org.springframework.data.redis.core.removeAndAwait
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author TheBIGMountain
 */
@Service
class CartServiceImpl(
  @Value("#{@productRepository}")
  private val productRepository: ProductRepository,
  @Value("#{@reactiveStringRedisTemplate}")
  private val redisTemplate: ReactiveStringRedisTemplate
): CartService {
  private companion object {
    const val CART_REDIS_KEY_TEMPLATE = "cart"
    const val quantity = 1
    val gson = Gson()
  }

  /**
   * 购物车添加商品
   */
  override fun add(uid: Int, form: CartAddForm): Flow<ResponseVo<CartVo>> {
    return productRepository.findById(form.productId!!).asFlow()
      // 商品是否存在
      .onEmpty { throw BusinessException(PRODUCT_NOT_EXIST) }
      // 商品是否正常在售
      .onEach { if (it.status != ON_SALE.code) throw BusinessException(PRODUCT_OFF_SALE_OR_DELETE) }
      // 商品库存是否充足
      .onEach { if (it.stock!! <= 0) throw BusinessException(PRODUCT_STOCK_ERROR) }
      // 写入redis
      .onEach { product ->
        // 获取redis执行hash操作所需对象
        val redisKey = "${CART_REDIS_KEY_TEMPLATE}_${uid}"
        val hashTemplate = redisTemplate.opsForHash<String, String>()
        // 获取redis数据
        hashTemplate.get(redisKey, product.id!!.toString()).asFlow()
          // 如果不存在, 则格式化成json数据后存入redis
          .onEmpty {
            val cart = gson.toJson(Cart(product.id!!, quantity, form.selected))
            hashTemplate.put(redisKey, product.id!!.toString(), cart).asFlow().collect()
            // 如果存在, 反序列化json数据
          }.map { gson.fromJson(it, Cart::class.java) }
          // 数量 + 1
          .onEach { it.quantity += quantity }
          // 重新存入redis
          .collect { hashTemplate.put(redisKey, product.id!!.toString(), gson.toJson(it)).asFlow().collect() }
        // 返回结果
      }.map { findCartList(uid).single().toResponseVo() }
  }

  /**
   * 获取购物车列表
   *
   * @param uid 用户id
   */
  override fun list(uid: Int): Flow<ResponseVo<CartVo>> {
    return findCartList(uid).map { it.toResponseVo() }
  }

  /**
   * 更新购物车列表
   */
  override fun update(uid: Int, productId: Int, form: CartUpdateForm): Flow<ResponseVo<CartVo>> {
    // 获取redis执行hash操作所需对象
    val redisKey = "${CART_REDIS_KEY_TEMPLATE}_$uid"
    val hashTemplate = redisTemplate.opsForHash<String, String>()

    // 获取redis数据
    return hashTemplate.get(redisKey, productId.toString()).asFlow()
      // 如果不存在, 则抛出
      .onEmpty { throw BusinessException(CART_PRODUCT_NOT_EXIST) }
      // 如果存在, 反序列化json数据
      .map { gson.fromJson(it, Cart::class.java) }
      // 修改商品数量, 如果有的话
      .onEach { cart -> form.quantity.let { if (it != null && it > 0) cart.quantity = it } }
      // 修改商品是否选中, 如果有的话
      .onEach { cart -> form.selected.let { if (it != null) cart.productSelected = it } }
      // 重新存入redis
      .onEach { hashTemplate.put(redisKey, productId.toString(), gson.toJson(it)).asFlow().collect() }
      // 返回结果
      .map { findCartList(uid).single().toResponseVo() }
  }

  /**
   * 删除指定购物车中的商品
   */
  override fun delete(uid: Int, productId: Int): Flow<ResponseVo<CartVo>> {
    // 获取redis执行hash操作所需对象
    val redisKey = "${CART_REDIS_KEY_TEMPLATE}_$uid"
    val hashTemplate = redisTemplate.opsForHash<String, String>()

    // 获取redis数据
    return hashTemplate.get(redisKey, productId.toString()).asFlow()
      // 如果不存在, 则抛出
      .onEmpty { throw BusinessException(CART_PRODUCT_NOT_EXIST) }
      // 如果存在, 删除该商品
      .onEach { hashTemplate.removeAndAwait(redisKey, productId.toString()) }
      // 返回结果
      .map { findCartList(uid).single().toResponseVo() }
  }

  /**
   * 全选
   */
  override fun selectAll(uid: Int): Flow<ResponseVo<CartVo>> {
    return flowOf(findCartList(uid))
      .onEach { foreachProduct(uid) { it.second.productSelected = true }.collect() }
      .map { it.single().toResponseVo() }
  }

  /**
   * 全设为非选中
   */
  override fun unSelectAll(uid: Int): Flow<ResponseVo<CartVo>> {
    return flowOf(findCartList(uid))
      .onEach { foreachProduct(uid) { it.second.productSelected = false }.collect() }
      .map { it.single().toResponseVo() }
  }

  /**
   * 商品数量总和
   */
  override fun sum(uid: Int): Flow<ResponseVo<Int>> {
    return flowOf(AtomicInteger(0))
      .onEach { sum -> foreachProduct(uid) { sum.addAndGet(it.second.quantity) }.collect() }
      .map { it.get().toResponseVo() }
  }

  /**
   * 遍历购物车商品
   *
   * @param uid 用户id
   * @param ops 遍历过程需要执行的操作
   */
  override fun foreachProduct(uid: Int, ops: (Pair<String, Cart>) -> Unit): Flow<Unit> {
    // 获取redis执行hash操作所需对象
    val redisKey = "${CART_REDIS_KEY_TEMPLATE}_$uid"
    val hashTemplate = redisTemplate.opsForHash<String, String>()

    // 遍历购物车商品
    return hashTemplate.entriesAsFlow(redisKey)
      .onEmpty { throw BusinessException(PRODUCT_NOT_EXIST) }
      // 反序列化json数据
      .map { it.key to gson.fromJson(it.value, Cart::class.java) }
      // 执行传入的操作
      .onEach { ops(it) }
      // 序列化为json数据
      .map { it.first to gson.toJson(it.second) }
      // 存回redis中
      .map { hashTemplate.put(redisKey, it.first, it.second).asFlow().collect() }
  }

  private fun findCartList(uid: Int): Flow<CartVo> {
    // 是否全选
    var selectAll = true
    // 购物车数量
    var cartTotalQuantity = 0
    // 购物车总金额
    var cartTotalPrice = "0".toBigDecimal()

    return flowOf("${CART_REDIS_KEY_TEMPLATE}_$uid")
      // 获取redis执行hash操作所需对象, 执行查询操作
      .zip(flowOf(redisTemplate.opsForHash<String, String>())) { redisKey, hashTemplate ->
        hashTemplate.entries(redisKey).asFlow()
          // 购物车没有则取消全选
          .onEmpty { selectAll = false }
          // 反序列化json数据
          .map { it.key to gson.fromJson(it.value, Cart::class.java) }
          // 判断是否全选
          .onEach { if (!it.second.productSelected) selectAll = false }
          // 计算购物车数量
          .onEach { cartTotalQuantity += it.second.quantity }
          // 查询数据库中商品数据
          .map { pair ->
            productRepository.findByIdIsAndStatusIs(pair.first.toInt()).asFlow()
              .map { it.toCartProductVo(pair.second) }.single()
            // 计算购物车总价(只计算选中的商品)
          }.onEach { if (it.productSelected) cartTotalPrice += it.productTotalPrice } }
      // 装换结果
      .map { CartVo(it.toList(), selectAll, cartTotalPrice, cartTotalQuantity) }
  }
}
