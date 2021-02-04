package com.dqpi.mall.controller

import com.dqpi.mall.service.ProductService
import com.dqpi.mall.utils.ok
import com.dqpi.mall.utils.withPathVar
import com.dqpi.mall.utils.withQueryParam
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import javax.validation.ValidationException

/**
 * @author TheBIGMountain
 */
@RestController
class ProductController(
  @Value("#{@productServiceImpl}")
  private val productService: ProductService
) {

  suspend fun list(serverRequest: ServerRequest): ServerResponse {
    return flowOf(Unit)
      // 获取请求参数
      .withQueryParam(serverRequest, "categoryId" to "-1", "pageNum" to "1", "pageSize" to "10")
      // 查询商品列表
      .map {
        val categoryId = it.second[0].toInt()
        val pageNum = it.second[1].toInt()
        val pageSize = it.second[2].toInt()
        productService.list(categoryId, pageNum, pageSize).single()
      }.ok()
  }

  suspend fun detail(serverRequest: ServerRequest): ServerResponse {
    return flowOf(Unit)
      // 获取路径参数
      .withPathVar(serverRequest, "id")
      // 获取商品详情
      .map {
        val id = it.second.toIntOrNull() ?: throw ValidationException("路径参数有误")
        productService.detail(id).single()
      }.ok()
  }
}
