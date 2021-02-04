package com.dqpi.mall.vo

import com.dqpi.mall.entity.Shipping
import com.fasterxml.jackson.annotation.JsonInclude
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * @author TheBIGMountain
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class ResponseVo<T>(
  val status: Int,
  val msg: String? = null,
  val data: T? = null
)

class OrderVo(
  val orderNo: Long?,
  val payment: BigDecimal?,
  val paymentType: Int?,
  val postage: Int?,
  val status: Int?,
  val paymentTime: LocalDateTime?,
  val sendTime: LocalDateTime?,
  val endTime: LocalDateTime?,
  val closeTime: LocalDateTime?,
  val createTime: LocalDateTime?,
  val orderItemVoList: List<OrderItemVo>,
  val shippingId: Int?,
  val shippingVo: Shipping?
)

class OrderItemVo(
  val orderNo: Long?,
  val productId: Int?,
  val productName: String?,
  val productImage: String?,
  val currentUnitPrice: BigDecimal?,
  val quantity: Int?,
  val totalPrice: BigDecimal?,
  val createTime: LocalDateTime?,
)

class CartVo(
  val cartProductVoList: List<CartProductVo>,
  val selectAll: Boolean,
  val cartTotalPrice: BigDecimal = "0".toBigDecimal(),
  val cartTotalQuantity:  Int = 0
)

class CartProductVo(
  val productId: Int,
  val quantity: Int,
  val productName: String,
  val productSubtitle: String,
  val productMainImage: String,
  val productPrice: BigDecimal,
  val productStatus: Int,
  val productTotalPrice: BigDecimal,
  val productStock: Int,
  val productSelected: Boolean
)

class ProductVo(
  val id: Int,
  val categoryId: Int,
  val name: String,
  val subtitle: String,
  val mainImage: String,
  val status: Int,
  val price: BigDecimal
)

class ProductDetailVo(
  val id: Int,
  val categoryId: Int,
  val name: String,
  val subtitle: String,
  val mainImage: String,
  val subImages: String,
  val detail: String,
  val price: BigDecimal,
  val stock: Int,
  val status: Int,
  val createTime: LocalDateTime,
  val updateTime: LocalDateTime
)

class PageVo<T>(
  val pageNum: Int,
  val pageSize: Int,
  val current: Int,
  val content: T
)

class CategoryVo(
  val id: Int? = null,
  val parentId: Int? = null,
  val name: String? = null,
  val status: Int? = null,
  val sortOrder: Int = 0,
  var subCategories: MutableSet<CategoryVo> = TreeSet()
): Comparable<CategoryVo> {
  override fun compareTo(other: CategoryVo): Int {
    return if (sortOrder > other.sortOrder) -1 else 1
  }
}




