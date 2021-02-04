package com.dqpi.pay.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("mall_pay_info")
data class PayInfo(
  @Id
  val id: Int? = null,
  var userId: Int? = null,
  var orderNo: Long? = null,
  var payPlatform: Int? = null,
  var platformNumber: String? = null,
  var platformStatus: String? = null,
  var payAmount: BigDecimal? = null,
  var createTime: LocalDateTime? = null,
  var updateTime: LocalDateTime? = null
)
