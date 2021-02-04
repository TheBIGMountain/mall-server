package com.dqpi.mall.utils

import com.dqpi.mall.consts.OrderStatusEnum
import com.dqpi.mall.consts.PaymentTypeEnum
import com.dqpi.mall.consts.RoleEnum
import com.dqpi.mall.entity.*
import com.dqpi.mall.vo.*
import org.springframework.util.DigestUtils
import java.math.BigDecimal

fun Order.toOrderVo(orderItemList: List<OrderItem>, shipping: Shipping?) = OrderVo(
  orderNo = orderNo,
  payment = payment,
  paymentType = paymentType,
  postage = postage,
  status = status,
  paymentTime = paymentTime,
  endTime = endTime,
  sendTime = sendTime,
  closeTime = closeTime,
  createTime = createTime,
  orderItemVoList = orderItemList.map(OrderItem::toOrderItemVo),
  shippingId = shipping?.id,
  shippingVo = shipping
)

fun OrderItem.toOrderItemVo() = OrderItemVo(
  orderNo = orderNo,
  productId = productId,
  productName = productName,
  productImage = productImage,
  currentUnitPrice = currentUnitPrice,
  quantity = quantity,
  totalPrice = totalPrice,
  createTime = createTime
)

fun List<OrderItem>.toOrder(uid: Int, orderNo: Long, shippingId: Int) = Order(
  orderNo = orderNo,
  userId = uid,
  shippingId = shippingId,
  paymentType = PaymentTypeEnum.PAY_ONLINE.code,
  postage = 0,
  status = OrderStatusEnum.NO_PAY.code,
  payment = mapNotNull(OrderItem::totalPrice).reduce(BigDecimal::add)
)

fun Product.toOrderItem(uid: Int, orderNo: Long, quantity: Int) = OrderItem(
  userId = uid,
  orderNo = orderNo,
  productId = id,
  productName = name,
  productImage = mainImage,
  currentUnitPrice = price,
  quantity = quantity,
  totalPrice = price!! * quantity.toBigDecimal()
)

fun ShippingForm.toShipping(uid: Int) = Shipping(
  userId = uid,
  receiverName = receiverName,
  receiverPhone = receiverPhone,
  receiverMobile = receiverMobile,
  receiverProvince = receiverProvince,
  receiverCity = receiverCity,
  receiverDistrict = receiverDistrict,
  receiverAddress = receiverAddress,
  receiverZip = receiverZip,
)

fun Product.toCartProductVo(cart: Cart) = CartProductVo(
  productId = id!!,
  quantity = cart.quantity,
  productName = name!!,
  productSubtitle = subtitle!!,
  productMainImage = mainImage!!,
  productPrice = price!!,
  productStatus = status!!,
  productTotalPrice = price!! * cart.quantity.toBigDecimal(),
  productStock = stock!!,
  productSelected = cart.productSelected
)

fun Category.toCategoryVo() = CategoryVo(
  id = id,
  parentId = parentId,
  name = name,
  status = status,
  sortOrder = sortOrder!!,
)

fun UserRegisterForm.toUser() = User(
  username = username,
  password = DigestUtils.md5DigestAsHex(password!!.toByteArray(Charsets.UTF_8)),
  email = email,
  role = RoleEnum.CUSTOMER.code
)

fun Product.toProductVo() = ProductVo(
  id = id!!,
  categoryId = categoryId!!,
  name = name!!,
  subtitle = subtitle!!,
  mainImage = mainImage!!,
  status = status!!,
  price = price!!
)

fun Product.toProductDetailVo() = ProductDetailVo(
  id = id!!,
  categoryId = categoryId!!,
  name = name!!,
  subtitle = subtitle!!,
  mainImage = mainImage!!,
  subImages = subImages!!,
  detail = detail!!,
  price = price!!,
  stock = if (stock!! < 100) 100 else stock!!,
  status = status!!,
  createTime = createTime!!,
  updateTime = updateTime!!
)

fun <E, T: List<E>> T.toPageVo(pageNum: Int, pageSize: Int): PageVo<T> {
  return PageVo(
    pageNum = pageNum,
    pageSize = pageSize,
    current = size,
    content = this
  )
}

