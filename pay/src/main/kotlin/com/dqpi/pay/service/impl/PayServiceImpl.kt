package com.dqpi.pay.service.impl

import com.dqpi.pay.entity.PayInfo
import com.dqpi.pay.repository.PayInfoRepository
import com.dqpi.pay.service.PayService
import com.dqpi.pay.utils.toPayPlatformEnum
import com.lly835.bestpay.enums.BestPayPlatformEnum
import com.lly835.bestpay.enums.BestPayTypeEnum
import com.lly835.bestpay.enums.OrderStatusEnum
import com.lly835.bestpay.model.PayRequest
import com.lly835.bestpay.model.PayResponse
import com.lly835.bestpay.service.BestPayService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlow
import reactor.util.function.Tuples
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * @author TheBIGMountain
 */
@Service
class PayServiceImpl(
  @Value("#{@bestPayService}")
  private val bestPayService: BestPayService,
  @Value("#{@payInfoRepository}")
  private val payInfoRepository: PayInfoRepository
): PayService {
  companion object {
    private val log = LoggerFactory.getLogger(PayServiceImpl::class.java)
  }

  /**
   * 创建/发起支付
   *
   * @param orderId 订单号
   * @param amount  交易金额
   * @param payType 支付方式 -> 微信或支付宝
   * @return 支付结果
   */
  override fun create(orderId: String, amount: BigDecimal,
                      payType: BestPayTypeEnum): Flow<PayResponse> {
    // 存入订单信息
    val saveResult = payInfoRepository.findByOrderNoIs(orderId.toLong()).asFlow()
      // 若数据库不存在该订单, 则创建订单
      .onEmpty {
        PayInfo().also {
          it.orderNo = orderId.toLong()
          it.payPlatform = payType.toPayPlatformEnum().code
          it.platformStatus = OrderStatusEnum.NOTPAY.name
          it.payAmount = amount
        }.also { emit(it) }
        // 存入数据库
      }.map { payInfoRepository.save(it).awaitSingle() }
      // 输出存入数据信息
      .onEach { log.warn("数据库存入支付信息数据: $it") }

    val payResult = flowOf(PayRequest().also {
      // 配置支付请求参数
      it.orderId = orderId
      it.orderName = "6051588-订单名"
      it.orderAmount = amount.toDouble()
      it.payTypeEnum = payType
    }).map(bestPayService::pay)

    // 并发执行
    return saveResult.zip(payResult) { _, res -> res }
  }

  /**
   * 支付结果异步通知处理
   *
   * @param notifyData 支付结果通知数据
   * @return 告知支付平台已收到通知
   */
  override fun asyncNotify(notifyData: String): Flow<String> {
    // 签名检验
    return flowOf(bestPayService.asyncNotify(notifyData))
      // 获取数据库中数据
      .map { Tuples.of(it, payInfoRepository.findByOrderNoIs(it.orderId.toLong()).awaitSingle()) }
      // 金额校验
      .onEach {
        log.warn("金额校验中数据库订单数据: ${it.t1}")
        if (it.t2.platformStatus != OrderStatusEnum.SUCCESS.name) {
          if (it.t2.payAmount!!.compareTo(it.t1.orderAmount.toBigDecimal()) != 0) {
            throw RuntimeException("异步通知中的金额和数据库不一致, orderId=${it.t1.orderId}")
          }
        }
        // 修改订单支付状态, 并存入数据库
      }.onEach {
        it.t2.platformStatus = OrderStatusEnum.SUCCESS.name
        it.t2.platformNumber = it.t1.outTradeNo
        it.t2.updateTime = LocalDateTime.now()
        log.warn("订单修改状态后: ${payInfoRepository.save(it.t2).awaitSingle()}")
        // 订单校验中出现问题则抛出
      }.catch { throw RuntimeException("金额校验中，查询到的订单出现问题: ${it.message}") }
      // 发送订单信息
      .map {
        withContext(Dispatchers.IO) {
          // 发起请求修改订单为已支付
          launch {
            WebClient.create()
              .post()
              .uri("http://localhost:8080/payNotify")
              .bodyValue(it.t2)
              .retrieve()
              .bodyToFlow<Unit>().collect()
          }
          // 告知支付平台已成功通知
          it.t1.notifyPlatForm()
        }
      }
  }

  /**
   * 根据订单号查询订单信息
   *
   * @param orderId 订单号
   * @return 订单信息
   */
  override fun queryByOrderId(orderId: String): Flow<PayInfo>
          = payInfoRepository.findByOrderNoIs(orderId.toLong()).asFlow()


  /**
   * 通知相应平台已成功获取通知后, 所需的响应结果
   */
  private fun PayResponse.notifyPlatForm()
          = when(payPlatformEnum) {
    BestPayPlatformEnum.WX -> """
            <xml>
                <return_code><![CDATA[SUCCESS]]></return_code>
                <return_msg><![CDATA[OK]]></return_msg>
            </xml>
          """.trimIndent()
    BestPayPlatformEnum.ALIPAY -> "success"
    else -> throw RuntimeException("异步通知中错误的支付平台")
  }
}




