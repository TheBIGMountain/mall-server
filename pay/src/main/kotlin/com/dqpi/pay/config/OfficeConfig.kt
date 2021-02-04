package com.dqpi.pay.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("alipay")
class AliConfig(
  val appId: String,
  val privateKey: String,
  val aliPayPublicKey: String,
  val notifyUrl: String,
  val returnUrl: String
)

@ConstructorBinding
@ConfigurationProperties("wx")
class WxConfig(
  val appId: String,
  val mchId: String,
  val mchKey: String,
  val notifyUrl: String,
  val returnUrl: String
)