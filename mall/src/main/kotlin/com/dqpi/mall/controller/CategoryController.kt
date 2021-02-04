package com.dqpi.mall.controller

import com.dqpi.mall.service.CategoryService
import com.dqpi.mall.service.OrderService
import com.dqpi.mall.utils.ok
import com.dqpi.pay.entity.PayInfo
import kotlinx.coroutines.flow.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToFlow
import org.springframework.web.reactive.function.server.buildAndAwait

/**
 * @author TheBIGMountain
 */
@RestController
class CategoryController(
  @Value("#{@categoryServiceImpl}")
  private val categoryService: CategoryService,
  @Value("#{@orderServiceImpl}")
  private val orderService: OrderService
) {
  /**
   * 查询所有商品目录
   */
  suspend fun findAll(serverRequest: ServerRequest): ServerResponse {
    return categoryService.findAll().ok()
  }

  /**
   * 获取支付成功通知
   */
  suspend fun payNotify(serverRequest: ServerRequest): ServerResponse {
    return serverRequest.bodyToFlow<PayInfo>()
      .filter { it.platformStatus == "SUCCESS" }
      .onEach { orderService.paid(it.orderNo!!).collect() }
      .map { ServerResponse.ok().buildAndAwait() }.single()
  }
}