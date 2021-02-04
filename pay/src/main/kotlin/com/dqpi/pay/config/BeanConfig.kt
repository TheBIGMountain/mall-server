package com.dqpi.pay.config

import com.lly835.bestpay.config.AliPayConfig
import com.lly835.bestpay.config.WxPayConfig
import com.lly835.bestpay.service.impl.BestPayServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author TheBIGMountain
 */
@Configuration
class BeanConfig {
  /**
   * 发起支付所需官方配置
   */
  @Bean
  fun bestPayService(wxPayConfig: WxPayConfig, aliPayConfig: AliPayConfig) = BestPayServiceImpl().also {
    it.setWxPayConfig(wxPayConfig)
    it.setAliPayConfig(aliPayConfig)
  }


  /**
   * 微信支付官方所需配置信息
   */
  @Bean
  fun wxPayConfig(wxConfig: WxConfig) = WxPayConfig().also {
    it.appId = wxConfig.appId
    it.mchId = wxConfig.mchId
    it.mchKey = wxConfig.mchKey
    it.notifyUrl = wxConfig.notifyUrl
    it.returnUrl = wxConfig.returnUrl
  }

  /**
   * 支付宝支付官方所需配置信息
   */
  @Bean
  fun aliPayConfig(aliConfig: AliConfig) = AliPayConfig().also {
    it.appId = aliConfig.appId
    it.privateKey = aliConfig.privateKey
    it.aliPayPublicKey = aliConfig.aliPayPublicKey
    it.notifyUrl = aliConfig.notifyUrl
    it.returnUrl = aliConfig.returnUrl
  }
}
