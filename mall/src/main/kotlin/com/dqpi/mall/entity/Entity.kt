package com.dqpi.mall.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

data class Cart(
  var productId: Int,
  var quantity: Int,
  var productSelected: Boolean = false
)

@Table("mall_category")
data class Category(
  @Id
  var id: Int? = null,
  var parentId: Int? = null,
  var name: String? = null,
  var status: Int? = null,
  var sortOrder: Int? = null,
  var createTime: LocalDateTime? = null,
  var updateTime: LocalDateTime? = null
)

@Table("mall_order")
data class Order(
  @Id
  var id: Int? = null,
  var orderNo: Long? = null,
  var userId: Int? = null,
  var shippingId: Int? = null,
  var payment: BigDecimal? = null,
  var paymentType: Int? = null,
  var postage: Int? = null,
  var status: Int? = null,
  var paymentTime: LocalDateTime? = null,
  var sendTime: LocalDateTime? = null,
  var endTime: LocalDateTime? = null,
  var closeTime: LocalDateTime? = null,
  var createTime: LocalDateTime? = null,
  var updateTime: LocalDateTime? = null
)

@Table("mall_order_item")
data class OrderItem(
  @Id
  var id: Int? = null,
  var userId: Int? = null,
  var orderNo: Long? = null,
  var productId: Int? = null,
  var productName: String? = null,
  var productImage: String? = null,
  var currentUnitPrice: BigDecimal? = null,
  var quantity: Int? = null,
  var totalPrice: BigDecimal? = null,
  var createTime: LocalDateTime? = null,
  var updateTime: LocalDateTime? = null
)

@Table("mall_product")
data class Product(
  @Id
  var id: Int? = null,
  var categoryId: Int? = null,
  var name: String? = null,
  var subtitle: String? = null,
  var mainImage: String? = null,
  var subImages: String? = null,
  var detail: String? = null,
  var price: BigDecimal? = null,
  var stock: Int? = null,
  var status: Int? = null,
  var createTime: LocalDateTime? = null,
  var updateTime: LocalDateTime? = null
)

@Table("mall_shipping")
data class Shipping(
  @Id
  var id: Int? = null,
  var userId: Int? = null,
  var receiverName: String? = null,
  var receiverPhone: String? = null,
  var receiverMobile: String? = null,
  var receiverProvince: String? = null,
  var receiverCity: String? = null,
  var receiverDistrict: String? = null,
  var receiverAddress: String? = null,
  var receiverZip: String? = null,
  var createTime: LocalDateTime? = null,
  var updateTime: LocalDateTime? = null
)

@Table("mall_user")
data class User(
  @Id
  var id: Int? = null,
  var username: String? = null,
  var password: String? = null,
  var email: String? = null,
  var phone: String? = null,
  var question: String? = null,
  var answer: String? = null,
  var role: Int? = null,
  var createTime: LocalDateTime? = null,
  var updateTime: LocalDateTime? = null
)