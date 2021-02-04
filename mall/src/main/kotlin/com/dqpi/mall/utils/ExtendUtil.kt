package com.dqpi.mall.utils

import com.dqpi.mall.consts.ResponseEnum
import com.dqpi.mall.exceptions.BusinessException
import com.dqpi.mall.vo.ResponseVo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import reactor.kotlin.core.publisher.toFlux
import javax.validation.ValidationException
import javax.validation.Validator

fun <T> String.toResponseVo(enum: ResponseEnum): ResponseVo<T> = ResponseVo(enum.code, this)

fun ResponseEnum.toResponseVo(): ResponseVo<Unit> = ResponseVo(code, msg)

fun <T> T.toResponseVo() = ResponseVo(ResponseEnum.SUCCESS.code, data = this)

suspend fun ResponseEnum.toServerResponse() = toResponseVo().toServerResponse()

suspend fun <T> ResponseVo<T>.toServerResponse() = ServerResponse.ok().bodyValueAndAwait(this)

fun <T> Flow<T>.valid(validator: Validator): Flow<T> {
  return onEmpty { throw BusinessException(ResponseEnum.PARAM_ERROR) }
    .onEach { target ->
      validator.validate(target).let {
        if (it.isEmpty()) return@let
        it.asFlow().map { validData -> validData.message }
          .reduce { msg, another -> "$msg, $another" }
          .let { err -> throw ValidationException(err) }
      }
    }
}

fun <T> Flow<T>.withUid(serverRequest: ServerRequest, redisTemplate: ReactiveRedisTemplate<String, String>)
: Flow<Pair<T, Int>> {
  return serverRequest.cookies()["token"]!!.toFlux()
    .flatMap { redisTemplate.opsForValue()[it.value] }
    .collectList()
    .map { it[0] }.asFlow()
    .zip(map { it }) { id, t -> t to id.toInt() }
}

fun <T> Flow<T>.withPathVar(serverRequest: ServerRequest, pathVar: String): Flow<Pair<T, String>> {
  return map { it to (serverRequest.pathVariable(pathVar)) }
}

fun <T> Flow<T>.withQueryParam(serverRequest: ServerRequest, vararg queryParam: Pair<String, String>): Flow<Pair<T, Array<String>>> {
  return map {
    val queryList =  queryParam.asFlow()
      .map { p -> serverRequest.queryParam(p.first).orElse(p.second) }.toList()
    it to queryList.toTypedArray()
  }.catch { throw ValidationException("请求参数有误") }
}

suspend fun <T> Flow<ResponseVo<T>>.ok(): ServerResponse {
  return map { it.toServerResponse() }
    .catch { it.catchException(this) }.single()
}

suspend fun Flow<ServerResponse>.catch(): ServerResponse {
  return catch { it.catchException(this) }.single()
}

private suspend fun Throwable.catchException(f: FlowCollector<ServerResponse>) {
  when(this) {
    is ValidationException -> f.emit(message!!.toResponseVo<Unit>(ResponseEnum.PARAM_ERROR).toServerResponse())
    is BusinessException -> f.emit(enum.toServerResponse())
    else -> f.emit(ResponseEnum.ERROR.toServerResponse())
  }
}



