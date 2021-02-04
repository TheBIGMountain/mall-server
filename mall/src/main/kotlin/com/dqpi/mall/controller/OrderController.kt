package com.dqpi.mall.controller

import com.dqpi.mall.service.OrderService
import com.dqpi.mall.utils.*
import com.dqpi.mall.vo.OrderCreateForm
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
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
class OrderController(
  @Value("#{@orderServiceImpl}")
  private val orderService: OrderService,
  @Value("#{@defaultValidator}")
  private val validator: Validator,
  private val redisTemplate: ReactiveRedisTemplate<String, String>
) {

  suspend fun create(serverRequest: ServerRequest): ServerResponse {
    return serverRequest.bodyToFlow<OrderCreateForm>()
      // 数据校验
      .valid(validator)
      // 获取uid
      .withUid(serverRequest, redisTemplate)
      // 创建订单
      .map { orderService.create(it.second, it.first.shippingId!!).single() }.ok()
  }

  suspend fun list(serverRequest: ServerRequest): ServerResponse {
    return emptyFlow<Unit>().onEmpty { emit(Unit) }
      // 获取uid
      .withUid(serverRequest, redisTemplate)
      // 获取分页参数
      .withQueryParam(serverRequest, "pageNum" to "1", "pageSize" to "10")
      // 获取订单列表
      .map {
        val uid = it.first.second
        val pageNum = it.second[0].toInt()
        val pageSize = it.second[1].toInt()
        orderService.list(uid, pageNum, pageSize).single()
      }.ok()
  }

  suspend fun detail(serverRequest: ServerRequest): ServerResponse {
    return emptyFlow<Unit>().onEmpty { emit(Unit) }
      // 获取uid
      .withUid(serverRequest, redisTemplate)
      // 获取路径里的订单号
      .withPathVar(serverRequest, "orderNo")
      // 获取订单详情
      .map {
        val orderNo = it.second.toLongOrNull() ?: throw ValidationException("路径参数有误")
        orderService.detail(it.first.second, orderNo).single()
      }.ok()
  }


  suspend fun cancel(serverRequest: ServerRequest): ServerResponse {
    return emptyFlow<Unit>().onEmpty { emit(Unit) }
      // 获取uid
      .withUid(serverRequest, redisTemplate)
      // 获取路径里的订单号
      .withPathVar(serverRequest, "orderNo")
      // 取消订单
      .map {
        val orderNo = it.second.toLongOrNull() ?: throw ValidationException("路径参数有误")
        orderService.cancel(it.first.second, orderNo).single()
      }.ok()
  }
}