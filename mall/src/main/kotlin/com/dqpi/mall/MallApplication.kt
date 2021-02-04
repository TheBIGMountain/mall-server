package com.dqpi.mall

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.reactive.TransactionalOperator
import redis.embedded.RedisServer
import javax.annotation.PreDestroy

@SpringBootApplication
class MallApplication {
  @Value("#{@redisServer}")
  private lateinit var redisServer: RedisServer
  @Value("#{@r2dbcDatabaseClient}")
  private lateinit var databaseClient: DatabaseClient

  @Bean
  fun init() = ApplicationRunner {
    // 执行sql语句
    ClassPathResource("sql.txt").inputStream.readAllBytes().let {
      databaseClient.sql(it.toString(Charsets.UTF_8)).then().subscribe()
    }
    // 启动内嵌redis
    redisServer.start()
  }

  @Bean
  fun transactionalOperator(): TransactionalOperator
  = TransactionalOperator.create(R2dbcTransactionManager(databaseClient.connectionFactory))

  /**
   * 添加内嵌redis
   */
  @Bean
  fun redisServer() = RedisServer()

  /**
   * 关闭内嵌redis
   */
  @PreDestroy
  fun destroy() {
    redisServer.stop()
  }
}

fun main(args: Array<String>) {
  runApplication<MallApplication>(*args)
}
