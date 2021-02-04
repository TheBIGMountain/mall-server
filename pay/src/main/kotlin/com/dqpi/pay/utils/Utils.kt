package com.dqpi.pay.utils

import com.dqpi.pay.consts.PayPlatformEnum
import com.lly835.bestpay.enums.BestPayTypeEnum
import com.lly835.bestpay.enums.BestPayTypeEnum.getByName
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.single
import org.springframework.web.reactive.function.server.ServerRequest

/**
 * 获取url路径中的请求参数, 不存在时抛出异常
 */
fun ServerRequest.getParamOrThrow(name: String, msg: String): String {
  return queryParam(name).orElseThrow { throw RuntimeException(msg) }
}

/**
 * 转换所需的BestPayTypeEnum
 */
fun String.toPayType(): BestPayTypeEnum {
  return getByName(this)
}

/**
 * 转换所需的PayPlatformEnum
 */
suspend fun BestPayTypeEnum.toPayPlatformEnum(): PayPlatformEnum {
  return PayPlatformEnum.values().asFlow()
    .filter { it.name == platform.name }
    .onEmpty { throw RuntimeException("不支持的平台类型") }
    .single()
}

