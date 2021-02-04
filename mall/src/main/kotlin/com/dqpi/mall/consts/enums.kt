package com.dqpi.mall.consts

enum class RoleEnum(val code: Int){
  ADMIN(0),
  CUSTOMER(1)
}

enum class ProductEnum(val code: Int){
  ON_SALE(1),
  OFF_SALE(2),
  DELETE(3)
}

enum class PaymentTypeEnum(val code: Int){
  PAY_ONLINE(1)
}

enum class OrderStatusEnum(val code: Int, val msg: String) {
  CANCELED(0, "已取消"),
  NO_PAY(10, "未付款"),
  PAID(20, "已付款"),
  SHIPPED(40, "已发货"),
  TRADE_SUCCESS(50, "交易成功"),
  TRADE_CLOSE(60, "交易关闭")
}

enum class ResponseEnum(val code: Int, val msg: String){
  ERROR(-1, "服务端错误"),
  SUCCESS(0, "成功"),
  USERNAME_OR_PASSWORD_ERROR(1, "用户名或密码错误"),
  USERNAME_EXIST(2, "用户名已存在"),
  NEED_LOGIN(3, "用户未登录，请先登录"),
  PARAM_ERROR(4, "参数错误"),
  EMAIL_EXIST(5, "邮箱已存在"),
  PRODUCT_OFF_SALE_OR_DELETE(7, "商品下架或删除"),
  PRODUCT_NOT_EXIST(8, "商品不存在"),
  PRODUCT_STOCK_ERROR(9, "库存不正确"),
  CART_PRODUCT_NOT_EXIST(10, "购物车商品不存在"),
  ADDRESS_NOT_EXIST(11, "不存在的地址"),
  CART_SELECTED_IS_EMPTY(12, "请选择商品后下单"),
  ORDER_NOT_EXIST(13, "订单不存在"),
  ORDER_STATUS_ERROR(14, "订单状态有误")
}

