package com.dqpi.mall.vo

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class ShippingForm(
  @field:NotBlank(message = "收货人姓名不能为空")
  val receiverName: String?,
  @field:NotBlank(message = "收货人手机不能为空")
  val receiverPhone: String?,
  @field:NotBlank(message = "收货人移动设备不能为空")
  val receiverMobile: String?,
  @field:NotBlank(message = "收货人省份不能为空")
  val receiverProvince: String?,
  @field:NotBlank(message = "收货人城市不能为空")
  val receiverCity: String?,
  @field:NotBlank(message = "收货人区域不能为空")
  val receiverDistrict: String?,
  @field:NotBlank(message = "收货人地址不能为空")
  val receiverAddress: String?,
  @field:NotBlank(message = "收货人邮编不能为空")
  val receiverZip: String?
)

class UserRegisterForm(
  @field:NotBlank(message = "用户名不能为空")
  val username: String?,
  @field:NotBlank(message = "用户密码不能为空")
  val password: String?,
  val email: String = "TheBIGMountain@163.com",
  val code: String
)

class UserLoginForm(
  @field:NotBlank(message = "用户名不能为空")
  val username: String?,
  @field:NotBlank(message = "用户密码不能为空")
  val password: String?
)

class CartAddForm(
  @field:NotNull(message = "商品id不能为空")
  val productId: Int?,
  val selected: Boolean = true
)

class CartUpdateForm(
  val quantity: Int? = null,
  val selected: Boolean? = null
)

class OrderCreateForm(
  @field:NotNull(message = "地址id不能为空")
  val shippingId: Int?
)


