package com.dqpi.mall.exceptions

import com.dqpi.mall.consts.ResponseEnum

class BusinessException(val enum: ResponseEnum): RuntimeException() {
  override val message = enum.msg
}


