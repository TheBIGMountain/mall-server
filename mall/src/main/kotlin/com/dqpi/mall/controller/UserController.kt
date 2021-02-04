package com.dqpi.mall.controller

import com.dqpi.mall.consts.ResponseEnum.SUCCESS
import com.dqpi.mall.service.UserService
import com.dqpi.mall.utils.*
import com.dqpi.mall.vo.UserLoginForm
import com.dqpi.mall.vo.UserRegisterForm
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.http.ResponseCookie
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlow
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToFlow
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import reactor.kotlin.core.publisher.toFlux
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import javax.validation.ValidationException
import javax.validation.Validator


/**
 * @author TheBIGMountain
 */
@RestController
class UserController(
  @Value("#{@userServiceImpl}")
  private val userService: UserService,
  @Value("#{@defaultValidator}")
  private val validator: Validator,
  private val redisTemplate: ReactiveRedisTemplate<String, String>
) {

  private val codeMap = ConcurrentHashMap<String, String>()

  suspend fun verificationCode(serverRequest: ServerRequest): ServerResponse {
    return flow {
      emit(serverRequest.queryParam("phoneNumber").orElseThrow { ValidationException("请传入手机号码") } )
    }.onEach { phoneNumber ->
      GlobalScope.launch {
        val randomNode = ThreadLocalRandom.current().nextInt(1000, 10000)
        codeMap[phoneNumber] = randomNode.toString()
        WebClient.create("http://106.ihuyi.com/webservice/sms.php?method=Submit&account=C75643418&password=da8e52eadbe725ee430625f0fb1cab01&mobile=$phoneNumber&content=您的验证码是：$randomNode。请不要把验证码泄露给其他人。")
          .get()
          .retrieve()
          .bodyToFlow<String>()
          .onEach { delay(1000 * 60); codeMap.remove(phoneNumber) }
          .collect()
      }
    }.map { SUCCESS.toResponseVo() }.ok()
  }

  suspend fun register(serverRequest: ServerRequest): ServerResponse {
    return serverRequest.bodyToFlow<UserRegisterForm>()
      // 短信验证码验证
      .onEach {
        codeMap[it.username]?.let { code ->
          if (code != it.code) throw ValidationException("验证码错误")
          codeMap.remove(it.username)
        } ?: throw ValidationException("验证码错误")
      }
      // 数据校验
      .valid(validator)
      // 用户注册
      .map { userService.register(it).single() }.ok()
  }

  suspend fun login(serverRequest: ServerRequest): ServerResponse {
    return serverRequest.bodyToFlow<UserLoginForm>()
      // 数据校验
      .valid(validator)
      // 用户登录
      .map { userService.login(it.username!!, it.password!!).single() }
      // 存入redis
      .map {
        UUID.randomUUID().toString().also { uuid ->
          redisTemplate.opsForValue().set(uuid, "${it.data!!.id}").subscribe()
        }.let { uuid -> it to uuid }
      // 设置cookie
      }.map {
        val cookie = ResponseCookie.from("token", it.second).path("/").maxAge(1800).build()
        ServerResponse.ok().cookie(cookie).bodyValueAndAwait(it.first)
      }.catch()
  }

  suspend fun userInfo(serverRequest: ServerRequest): ServerResponse {
    return flowOf(Unit)
      // 获取存入redis中的用户id
      .withUid(serverRequest, redisTemplate)
      // 查询用户
      .map { userService.findOne(it.second).single() }.ok()
  }

  suspend fun logout(serverRequest: ServerRequest): ServerResponse {
    return serverRequest.cookies()["token"]!!.toFlux()
      .flatMap { redisTemplate.opsForValue()[it.value] }
      .collectList().asFlow()
      .onEach { redisTemplate.opsForValue().delete(it[0]).asFlow().collect() }
      .map { ResponseCookie.from("token", "").path("/").maxAge(0).build() }
      .map { ServerResponse.ok().cookie(it).bodyValueAndAwait(SUCCESS.toResponseVo()) }
      .catch()
  }
}





