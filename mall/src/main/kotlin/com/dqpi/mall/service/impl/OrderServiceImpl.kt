package com.dqpi.mall.service.impl

import com.dqpi.mall.consts.OrderStatusEnum.*
import com.dqpi.mall.consts.ProductEnum
import com.dqpi.mall.consts.ResponseEnum.*
import com.dqpi.mall.entity.*
import com.dqpi.mall.exceptions.BusinessException
import com.dqpi.mall.repository.OrderItemRepository
import com.dqpi.mall.repository.OrderRepository
import com.dqpi.mall.repository.ProductRepository
import com.dqpi.mall.repository.ShippingRepository
import com.dqpi.mall.service.CartService
import com.dqpi.mall.service.OrderService
import com.dqpi.mall.utils.*
import com.dqpi.mall.vo.OrderVo
import com.dqpi.mall.vo.PageVo
import com.dqpi.mall.vo.ResponseVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.transactional
import reactor.util.function.Tuple3
import reactor.util.function.Tuples
import java.time.LocalDateTime
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ThreadLocalRandom

/**
 * @author TheBIGMountain
 */
@Service
class OrderServiceImpl(
  @Value("#{@orderRepository}")
  private val orderRepository: OrderRepository,
  @Value("#{@shippingRepository}")
  private val shippingRepository: ShippingRepository,
  @Value("#{@cartServiceImpl}")
  private val cartService: CartService,
  @Value("#{@productRepository}")
  private val productRepository: ProductRepository,
  @Value("#{@orderItemRepository}")
  private val orderItemRepository: OrderItemRepository,
  @Value("#{@transactionalOperator}")
  private val transactionalOperator: TransactionalOperator
): OrderService {
  /**
   * 创建订单
   */
  override fun create(uid: Int, shippingId: Int): Flow<ResponseVo<OrderVo>> {
    val shipping = shippingRepository.findByIdIsAndUserIdIs(shippingId, uid).asFlow()
      // 地址校验
      .onEmpty { throw BusinessException(ADDRESS_NOT_EXIST) }

    val productList = flow {
      // 获取购物车商品
      val productList = getProductList(uid)
      // 生成订单编号
      val orderNo = System.currentTimeMillis() + ThreadLocalRandom.current().nextLong(999)
      // 采集订单项数据
      val orderItemList = productList.asFlow()
        .map { cart ->
          // 查询购物车中商品信息
          productRepository.findById(cart.productId).asFlow()
            // 商品校验
            .validProduct()
            // 库存校验, 并保存数据
            .validStock(cart)
            // 转换为订单列表项, 写入数据库
            .toOrderItem(uid, orderNo, cart)
            // 更新购物车
            .also { cartService.delete(uid, cart.productId).single() }
        }.toList()

      // 生成订单, 存入数据库
      orderItemList.toOrder(uid, orderNo, shippingId).let {
        orderRepository.save(it).awaitSingle()
        // 发送flow数据
      }.let { emit(it to orderItemList) }
    }


    // 并行处理, 然后返回结果, 并添加事务
    return shipping.zip(productList) { it, pair ->
      pair.first.toOrderVo(pair.second, it).toResponseVo()
    }.transactional(transactionalOperator)
  }

  /**
   * 获取订单列表
   */
  override fun list(uid: Int, pageNum: Int, pageSize: Int): Flow<ResponseVo<PageVo<List<OrderVo>>>> {
    return flowOf(orderRepository.findAllByUserIdIs(uid).asFlow())
      // 获取订单列表  -> List<Order>
      .withOrderList(pageNum, pageSize)
      // 获取订单项映射 -> Map<OrderNo, List<OrderItem>>
      .withOrderItemsMap()
      // 获取地址映射 -> Map<ShippingId, Shipping>
      .withShippingMap()
      // 转换数据 List<Order> -> List<OrderVo>
      // t1 -> List<Order>, t2 -> Map<OrderNo, List<OrderItem>>, t3 -> Map<ShippingId, Shipping>
      .map { tuple -> tuple.t1.map { it.toOrderVo(tuple.t2[it.orderNo]!!, tuple.t3[it.userId]!!) } }
      // 转换为分页数据
      .map { it.toPageVo(pageNum, pageSize).toResponseVo() }
  }

  /**
   * 获取订单详情
   */
  override fun detail(uid: Int, orderNo: Long): Flow<ResponseVo<OrderVo>> {
    return orderRepository.findByOrderNoIs(orderNo).asFlow()
      // 过滤非该用户的订单
      .filter { it.userId == uid }
      // 如果没有订单则抛出
      .onEmpty { throw BusinessException(ORDER_NOT_EXIST) }
      // 获取订单项Flow -> Flow<OrderItem>
      .map { it to orderItemRepository.findByOrderNoIs(orderNo).asFlow() }
      // 转换返回对象
      .map {
        // 并行获取订单项列表和地址
        flowOf(it.second).zip(shippingRepository.findById(it.first.shippingId!!).asFlow()) {
            orderItem, shipping -> it.first.toOrderVo(orderItem.toList(), shipping)
        }.single().toResponseVo()
      }
  }

  /**
   * 取消订单
   */
  override fun cancel(uid: Int, orderNo: Long): Flow<ResponseVo<Unit>> {
    return orderRepository.findByOrderNoIs(orderNo).asFlow()
      // 过滤非该用户的订单
      .filter { it.userId == uid }
      // 如果没有订单则抛出
      .onEmpty { throw BusinessException(ORDER_NOT_EXIST) }
      // 只有未付款订单可以取消
      .onEach { if (it.status != NO_PAY.code) throw BusinessException(ORDER_STATUS_ERROR) }
      // 修改订单属性
      .onEach {
        it.status = CANCELED.code
        it.closeTime = LocalDateTime.now()
        // 存入数据库
      }.onEach { orderRepository.save(it).asFlow().collect() }
      // 响应成功
      .map { SUCCESS.toResponseVo() }
  }

  /**
   * 订单支付完成
   */
  override fun paid(orderNo: Long): Flow<Unit> {
    return orderRepository.findByOrderNoIs(orderNo).asFlow()
      // 如果没有订单则抛出
      .onEmpty { throw BusinessException(ORDER_NOT_EXIST) }
      // 只有未付款订单可以取消
      .onEach { if (it.status != NO_PAY.code) throw BusinessException(ORDER_STATUS_ERROR) }
      // 修改订单属性
      .onEach {
        it.status = PAID.code
        it.paymentTime = LocalDateTime.now()
        // 存入数据库
      }.map { orderRepository.save(it).asFlow().collect() }
  }

  /**
   * 获取订单列表  -> List<Order>
   *
   * @return Flow<List<Order>>
   */
  private fun Flow<Flow<Order>>.withOrderList(pageNum: Int, pageSize: Int)
  : Flow<List<Order>> {
    // 筛选分页数据
    return map { it.drop((pageNum - 1) * pageSize).take(pageSize).toList() }
  }

  /**
   * 获取订单项映射 -> Map<OrderNo, List<OrderItem>>
   *
   * @return Flow<Pair<List<Order>, Flow<Map<OrderNo, List<OrderItem>>>>>
   */
  private fun Flow<List<Order>>.withOrderItemsMap()
          : Flow<Pair<List<Order>, Flow<Map<Long?, List<OrderItem>>>>> {
    return map { list ->
      // 生成Flow<Map<OrderNo, List<OrderItem>>>
      val orderItemMapFlow = flowOf(list.asFlow())
        .map {
          it.map { order -> order.orderNo }
            .map { no -> no to orderItemRepository.findByOrderNoIs(no!!).asFlow().toList()  }
            .toList().toMap()
        }
      // 构造结果为下流并发生成数据使用
      list to orderItemMapFlow
    }
  }

  /**
   * 获取地址映射 -> Map<ShippingId, Shipping>
   *
   * @return Flow<Tuple3<List<Order>, Map<OrderNo, List<OrderItem>>, Map<ShippingId, Shipping>>>
   */
  private fun Flow<Pair<List<Order>, Flow<Map<Long?, List<OrderItem>>>>>.withShippingMap()
          : Flow<Tuple3<List<Order>, Map<Long?, List<OrderItem>>, Map<Int?, Shipping>>> {
    return map { pair ->
      // 生成Flow<Map<ShippingId, Shipping>>
      val shippingMap = flowOf(pair.first.asFlow())
        .map {
          it.map { order -> order.shippingId }
            .map { order -> shippingRepository.findById(order!!).awaitSingle() }
            .toList().associateBy(Shipping::id)
        }
      // 并行处理生成订单项映射和地址映射
      val temp = pair.second.zip(shippingMap) { a, b -> a to b }.single()
      // 构造结果返回
      Tuples.of(pair.first, temp.first, temp.second)
    }
  }

  /**
   * 获取购物车商品
   */
  private suspend fun getProductList(uid: Int): CopyOnWriteArrayList<Cart> {
    // 存储购物车商品
    val productList = CopyOnWriteArrayList<Cart>()
    // 遍历购物车商品
    cartService.foreachProduct(uid) {
      if (it.second.productSelected) productList.add(it.second)
    }.collect()
    // 购物车空校验
    if (productList.isEmpty()) throw BusinessException(CART_SELECTED_IS_EMPTY)
    return productList
  }

  /**
   * 验证商品
   */
  private fun Flow<Product>.validProduct(): Flow<Product> {
    // 商品空校验
    return onEmpty { throw BusinessException(PRODUCT_NOT_EXIST) }
      // 商品上下架状态校验
      .onEach {
        if (it.status != ProductEnum.ON_SALE.code)
          throw BusinessException(PRODUCT_OFF_SALE_OR_DELETE)
      }
      // 前面操作交给其他线程调度
      .flowOn(Dispatchers.Default)
  }

  /**
   * 验证库存
   */
  private fun Flow<Product>.validStock(cart: Cart): Flow<Product> {
    // 库存校验
    return onEach {
      if (it.stock!! < cart.quantity)
        throw BusinessException(PRODUCT_STOCK_ERROR)
      // 减库存, 然后存入数据库
      productRepository.save(it.apply { stock = stock!! - 1 }).awaitSingle()
      // 前面操作交给其他线程调度
    }.flowOn(Dispatchers.Default)
  }


  private suspend fun Flow<Product>.toOrderItem(uid: Int, orderNo: Long, cart: Cart): OrderItem {
    // 转换为订单项, 写入数据库
    return map {
      val orderItem = it.toOrderItem(uid, orderNo, cart.quantity)
      orderItemRepository.save(orderItem).awaitSingle()
    }.single()
  }
}
