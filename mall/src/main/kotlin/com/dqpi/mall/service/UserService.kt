package com.dqpi.mall.service

import com.dqpi.mall.entity.User
import com.dqpi.mall.vo.ResponseVo
import com.dqpi.mall.vo.UserRegisterForm
import kotlinx.coroutines.flow.Flow

/**
 * @author TheBIGMountain
 */
interface UserService {

  /**
   * 用户注册
   *
   * @param userRegisterForm 前端传入的用户数据
   * @return 数据库保存后的用户数据
   */
  fun register(userRegisterForm: UserRegisterForm): Flow<ResponseVo<Unit>>

  /**
   * 用户登录
   *
   * @param username 用户名
   * @param password 用户密码
   * @return 用户信息
   */
  fun login(username: String, password: String): Flow<ResponseVo<User>>


  fun findOne(id: Int): Flow<ResponseVo<User>>
}