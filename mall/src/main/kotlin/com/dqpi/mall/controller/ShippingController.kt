package com.dqpi.mall.controller

import com.dqpi.mall.service.ShippingService
import com.dqpi.mall.utils.*
import com.dqpi.mall.vo.ShippingForm
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
class ShippingController(
  @Value("#{@shippingServiceImpl}")
  private val shippingService: ShippingService,
  @Value("#{@defaultValidator}")
  private val validator: Validator,
  private val redisTemplate: ReactiveRedisTemplate<String, String>
) {

  suspend fun add(serverRequest: ServerRequest): ServerResponse {
    return serverRequest.bodyToFlow<ShippingForm>()
      // 数据校验
      .valid(validator)
      // 获取uid
      .withUid(serverRequest, redisTemplate)
      // 添加地址
      .map { shippingService.add(it.second, it.first).single() }.ok()
  }

  suspend fun update(serverRequest: ServerRequest): ServerResponse {
    return serverRequest.bodyToFlow<ShippingForm>()
      // 获取uid
      .withUid(serverRequest, redisTemplate)
      // 获取shippingId
      .withPathVar(serverRequest, "shippingId")
      // 更新地址
      .map {
        val shippingId = it.second.toIntOrNull() ?: throw ValidationException("路径参数有误")
        shippingService.update(it.first.second, shippingId, it.first.first).single()
      }.ok()
  }

  suspend fun delete(serverRequest: ServerRequest): ServerResponse {
    return flowOf(Unit)
      // 获取uid
      .withUid(serverRequest, redisTemplate)
      // 获取shippingId
      .withPathVar(serverRequest, "shippingId")
      // 删除地址
      .map {
        val shippingId = it.second.toIntOrNull() ?: throw ValidationException("路径参数有误")
        shippingService.delete(it.first.second, shippingId).single()
      }.ok()
  }

  suspend fun list(serverRequest: ServerRequest): ServerResponse {
    return flowOf(Unit)
      // 获取uid
      .withUid(serverRequest, redisTemplate)
      // 获取请求参数
      .withQueryParam(serverRequest, "pageNum" to "1", "pageSize" to "10")
      // 获取分页地址数据
      .map {
        val uid = it.first.second
        val pageNum = it.second[0].toInt()
        val pageSize = it.second[1].toInt()
        shippingService.list(uid, pageNum, pageSize).single()
      }.ok()
  }
}

