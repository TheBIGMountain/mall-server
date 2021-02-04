package com.dqpi.pay.controller

import com.dqpi.pay.service.PayService
import com.dqpi.pay.utils.getParamOrThrow
import com.dqpi.pay.utils.toPayType
import com.lly835.bestpay.enums.BestPayTypeEnum
import com.lly835.bestpay.model.PayResponse
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.*
import reactor.util.function.Tuples

/**
 * @author TheBIGMountain
 */
@Controller
class PayController(
  @Value("#{@payServiceImpl}")
  private val payService: PayService,
  @Value("#{@environment.getProperty('wx.return-url')}")
  private val returnUrl: String
) {

  @Bean
  fun route() = coRouter {
    "/pay".nest {
      GET("/create", ::create)
      POST("/notify", ::asyncNotify)
      GET("/queryByOrderId", ::queryByOrderId)
    }
  }

  /**
   * 创建订单
   */
  suspend fun create(serverRequest: ServerRequest): ServerResponse {
    return flowOf(serverRequest).map {
      // 获取订单id, 待付款金额和支付平台类型(微信或支付宝), it -> 请求数据集合
      val orderId = it.getParamOrThrow("orderId", "订单号不能为空")
      val amount = it.getParamOrThrow("amount", "代付款金额不能为空").toBigDecimal()
      val payType = it.getParamOrThrow("payType", "支付平台类型不能为空").toPayType()
      Tuples.of(orderId, amount, payType)
      // 创建订单, 并发起支付
    }.map { Tuples.of(it.t3, payService.create(it.t1, it.t2, it.t3).single(), it.t1) }
      // 渲染支付结果
      .map { ServerResponse.ok().renderPayResponse(it.t1, it.t2) }
      // 支付失败返回404, it -> 异常数据集合
      .catch { emit(ServerResponse.notFound().buildAndAwait()) }.single()
  }

  /**
   * 响应支付平台发起的异步通知
   */
  suspend fun asyncNotify(serverRequest: ServerRequest): ServerResponse {
    return serverRequest.awaitBody<String>()
      // 检验支付平台异步通知, it -> 支付平台异步通知发送的信息
      .let { payService.asyncNotify(it) }
      // 通知平台, it -> 响应支付平台所需的数据
      .map { ServerResponse.ok().bodyValueAndAwait(it) }
      // it -> 异常数据集合
      .catch { emit(ServerResponse.badRequest().buildAndAwait()) }.single()
  }

  /**
   * 根据订单号查询订单
   */
  suspend fun queryByOrderId(serverRequest: ServerRequest): ServerResponse {
    return flowOf(serverRequest)
      .map { it.getParamOrThrow("orderId", "订单号不能为空") }
      .map(payService::queryByOrderId)
      .map { ServerResponse.ok().bodyAndAwait(it) }
      .catch {
        emit(ServerResponse.badRequest().bodyValueAndAwait("${it.message}"))
      }.single()
  }

  /**
   * 渲染支付平台所发送的结果
   */
  private suspend fun ServerResponse.BodyBuilder.renderPayResponse(
    payType: BestPayTypeEnum, payResponse: PayResponse
  ): ServerResponse
          = when(payType) {
    BestPayTypeEnum.WXPAY_NATIVE -> bodyValueAndAwait(payResponse.codeUrl)
    BestPayTypeEnum.ALIPAY_PC -> renderAndAwait("CreateForAlipay", mapOf("body" to payResponse.body))
    else -> throw RuntimeException("不支持的支付类型")
  }

}


