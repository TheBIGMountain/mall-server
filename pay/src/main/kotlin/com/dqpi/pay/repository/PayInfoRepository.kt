package com.dqpi.pay.repository

import com.dqpi.pay.entity.PayInfo
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

/**
 * @author TheBIGMountain
 */
interface PayInfoRepository: ReactiveCrudRepository<PayInfo, Int> {
  /**
   * 根据订单号查询订单
   *
   * @param orderNo 订单号
   * @return 支付订单信息
   */
  fun findByOrderNoIs(orderNo: Long): Mono<PayInfo>
}