package com.dqpi.mall.controller

import com.dqpi.mall.service.CartService
import com.dqpi.mall.utils.ok
import com.dqpi.mall.utils.valid
import com.dqpi.mall.utils.withPathVar
import com.dqpi.mall.utils.withUid
import com.dqpi.mall.vo.CartAddForm
import com.dqpi.mall.vo.CartUpdateForm
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToFlow
import javax.validation.ValidationException
import javax.validation.Validator

/**
 * @author TheBIGMountain
 */
@RestController
class CartController(
  @Value("#{@defaultValidator}")
  private val validator: Validator,
  @Value("#{@cartServiceImpl}")
  private val cartService: CartService,
  private val redisTemplate: ReactiveRedisTemplate<String, String>
) {

  suspend fun list(serverRequest: ServerRequest): ServerResponse {
    return flowOf(Unit)
      // 从session中获取uid
      .withUid(serverRequest, redisTemplate)
      // 响应结果
      .map { cartService.list(it.second).single() }.ok()
  }

  suspend fun add(serverRequest: ServerRequest): ServerResponse {
    return serverRequest.bodyToFlow<CartAddForm>()
      // 数据校验
      .valid(validator)
      // 获取uid
      .withUid(serverRequest, redisTemplate)
      // 添加购物车
      .map { cartService.add(it.second, it.first).single() }.ok()
  }

  suspend fun update(serverRequest: ServerRequest): ServerResponse {
    return serverRequest.bodyToFlow<CartUpdateForm>()
      // 数据校验
      .valid(validator)
      // 获取uid
      .withUid(serverRequest, redisTemplate)
      // 获取路径变量
      .withPathVar(serverRequest, "productId")
      // 更新购物车
      .map {
        val productId = it.second.toIntOrNull() ?: throw ValidationException("路径参数有误")
        cartService.update(it.first.second, productId, it.first.first).single()
      }.ok()
  }

  suspend fun delete(serverRequest: ServerRequest): ServerResponse {
    return flowOf(Unit)
      // 获取uid
      .withUid(serverRequest, redisTemplate)
      // 获取路径变量
      .withPathVar(serverRequest, "productId")
      // 删除指定商品
      .map {
        val productId = it.second.toIntOrNull() ?: throw ValidationException("路径参数有误")
        cartService.delete(it.first.second, productId).single()
      }.ok()

  }

  suspend fun selectAll(serverRequest: ServerRequest): ServerResponse {
    return flowOf(Unit)
      // 获取uid
      .withUid(serverRequest, redisTemplate)
      // 全选
      .map { cartService.selectAll(it.second).single() }.ok()
  }

  suspend fun unSelectAll(serverRequest: ServerRequest): ServerResponse {
    return flowOf(Unit)
      // 获取uid
      .withUid(serverRequest, redisTemplate)
      // 全不选
      .map { cartService.unSelectAll(it.second).single() }.ok()
  }

  suspend fun sum(serverRequest: ServerRequest): ServerResponse {
    return flowOf(Unit)
      // 获取uid
      .withUid(serverRequest, redisTemplate)
      // 购物车数量
      .map { cartService.sum(it.second).single() }.ok()
  }
}

