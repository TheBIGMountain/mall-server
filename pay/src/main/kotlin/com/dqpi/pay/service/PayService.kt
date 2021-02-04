package com.dqpi.pay.service

import com.dqpi.pay.entity.PayInfo
import com.lly835.bestpay.enums.BestPayTypeEnum
import com.lly835.bestpay.model.PayResponse
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

/**
 * @author TheBIGMountain
 *
 * 订单支付接口
 */
interface PayService {
  /**
   * 创建/发起支付
   *
   * @param orderId 订单号
   * @param amount  交易金额
   * @param payType 支付方式 -> 微信或支付宝
   * @return 支付结果
   */
  fun create(orderId: String, amount: BigDecimal,
             payType: BestPayTypeEnum): Flow<PayResponse>

  /**
   * 支付结果异步通知处理
   *
   * @param notifyData 支付结果通知数据
   * @return 告知支付平台已收到通知
   */
  fun asyncNotify(notifyData: String): Flow<String>

  /**
   * 根据订单号查询订单信息
   *
   * @param orderId 订单号
   * @return 订单信息
   */
  fun queryByOrderId(orderId: String): Flow<PayInfo>
}