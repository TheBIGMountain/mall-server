package com.dqpi.mall.config

import com.dqpi.mall.consts.ResponseEnum
import com.dqpi.mall.controller.*
import com.dqpi.mall.exceptions.BusinessException
import com.dqpi.mall.utils.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import reactor.kotlin.core.publisher.toFlux


/**
 * @author TheBIGMountain
 */
@Configuration
class RouteConfig(
  @Value("#{@userController}")
  private val userController: UserController,
  @Value("#{@categoryController}")
  private val categoryController: CategoryController,
  @Value("#{@productController}")
  private val productController: ProductController,
  @Value("#{@cartController}")
  private val cartController: CartController,
  @Value("#{@shippingController}")
  private val shippingController: ShippingController,
  @Value("#{@orderController}")
  private val orderController: OrderController,
  private val redisTemplate: ReactiveRedisTemplate<String, String>
) {

  @Bean
  fun route(): RouterFunction<ServerResponse> = coRouter {
    accept(MediaType.APPLICATION_JSON).nest {
      GET("/verificationCode", userController::verificationCode)
      POST("/user/register", userController::register)
      POST("/user/login", userController::login)
      GET("/category/findAll", categoryController::findAll)
      GET("/products", productController::list)
      GET("/products/detail/{id}", productController::detail)
      POST("/payNotify", categoryController::payNotify)
    }

    accept(MediaType.APPLICATION_JSON).nest {
      GET("/user", userController::userInfo)
      POST("/user/logout", userController::logout)
      GET("/carts", cartController::list)
      POST("/carts", cartController::add)
      PUT("/carts/selectAll", cartController::selectAll)
      PUT("/carts/unSelectAll", cartController::unSelectAll)
      PUT("/carts/{productId}", cartController::update)
      DELETE("/carts/{productId}", cartController::delete)
      GET("/carts/products/sum", cartController::sum)
      POST("/shippings", shippingController::add)
      DELETE("/shippings/{shippingId}", shippingController::delete)
      PUT("/shippings/{shippingId}", shippingController::update)
      GET("/shippings", shippingController::list)
      POST("/orders", orderController::create)
      GET("/orders", orderController::list)
      GET("/orders/{orderNo}", orderController::detail)
      PUT("/orders/{orderNo}", orderController::cancel)

      filter { serverRequest, next ->
        serverRequest.session().asFlow()
          // 验证cookie, 判断用户是否已登录
          .map { serverRequest.cookies()["token"] ?: throw BusinessException(ResponseEnum.NEED_LOGIN) }
          // 验证redis, 判断用户是否登录
          .onEach { tokens ->
            tokens.toFlux()
              .flatMap { redisTemplate.opsForValue()[it.value] }
              .collectList().asFlow()
              .onEach { if (it.isEmpty()) throw BusinessException(ResponseEnum.NEED_LOGIN) }
              .collect()
          // 已登录, 则放行
          }.map { next(serverRequest) }
          // 捕获未登录异常
          .catch()
      }
    }
  }
}

