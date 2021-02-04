package com.dqpi.mall.service.impl

import com.dqpi.mall.consts.ResponseEnum.*
import com.dqpi.mall.entity.User
import com.dqpi.mall.exceptions.BusinessException
import com.dqpi.mall.repository.UserRepository
import com.dqpi.mall.service.UserService
import com.dqpi.mall.utils.toResponseVo
import com.dqpi.mall.utils.toUser
import com.dqpi.mall.vo.ResponseVo
import com.dqpi.mall.vo.UserRegisterForm
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.DigestUtils

/**
 * @author TheBIGMountain
 */
@Service
class UserServiceImpl(
  @Value("#{@userRepository}")
  private val userRepository: UserRepository
): UserService {
  /**
   * 用户注册
   *
   * @param userRegisterForm 前端传入的用户数据
   */
  override fun register(userRegisterForm: UserRegisterForm): Flow<ResponseVo<Unit>> {
    return flowOf(userRegisterForm.username)
      // 检测用户名是否重复
      .map { userRepository.countByUsernameIs(it!!).awaitSingle() }
      .onEach { if (it > 0) throw BusinessException(USERNAME_EXIST) }
      // 检测邮箱是否重复
      .map { userRepository.countByEmailIs(userRegisterForm.email!!).awaitSingle() }
      .onEach { if (it > 0) throw BusinessException(EMAIL_EXIST) }
      // 密码加密后存入数据库
      .map { userRepository.save(userRegisterForm.toUser()).asFlow().collect() }
      // 转换返回结果
      .map { SUCCESS.toResponseVo() }
  }

  /**
   * 用户登录
   *
   * @param username 用户名
   * @param password 用户密码
   * @return 用户信息
   */
  override fun login(username: String, password: String): Flow<ResponseVo<User>> {
    return flowOf(username)
      .map { userRepository.findByUsernameIs(it).awaitSingleOrNull() }
      // 不存在该用户则抛出
      .onEach { if (it == null) throw BusinessException(USERNAME_OR_PASSWORD_ERROR) }
      // 密码验证
      .onEach {
        val psw = password.toByteArray(Charsets.UTF_8)
        if (!it.password.equals(DigestUtils.md5DigestAsHex(psw), true))
          throw BusinessException(USERNAME_OR_PASSWORD_ERROR)
        // 隐藏密码
      }.onEach { it.password = "" }
      // 转换返回结果
      .map { it.toResponseVo() }
  }

  override fun findOne(id: Int): Flow<ResponseVo<User>> {
    return userRepository.findById(id).asFlow()
      .onEach { it.password = "" }
      .map { it.toResponseVo() }
  }
}


