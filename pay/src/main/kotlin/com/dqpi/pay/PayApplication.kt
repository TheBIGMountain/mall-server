package com.dqpi.pay

import com.dqpi.pay.config.AliConfig
import com.dqpi.pay.config.WxConfig
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.core.DatabaseClient

@SpringBootApplication
@EnableConfigurationProperties(WxConfig::class, AliConfig::class)
class PayApplication {
  /**
   * 初始化内嵌数据库
   */
  @Bean
  fun init(databaseClient: DatabaseClient) = ApplicationRunner {
    ClassPathResource("sql.txt").inputStream.readAllBytes().let {
      databaseClient.sql(it.toString(Charsets.UTF_8)).then().subscribe()
    }
  }

}

fun main(args: Array<String>) {
  runApplication<PayApplication>(*args)
}

