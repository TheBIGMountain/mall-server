package com.dqpi.mall.service

import com.dqpi.mall.vo.OrderVo
import com.dqpi.mall.vo.PageVo
import com.dqpi.mall.vo.ResponseVo
import kotlinx.coroutines.flow.Flow

/**
 * @author TheBIGMountain
 */
interface OrderService {

  /**
   * 创建订单
   */
  fun create(uid: Int, shippingId: Int): Flow<ResponseVo<OrderVo>>

  /**
   * 获取订单列表
   */
  fun list(uid: Int, pageNum: Int, pageSize: Int): Flow<ResponseVo<PageVo<List<OrderVo>>>>

  /**
   * 获取订单详情
   */
  fun detail(uid: Int, orderNo: Long): Flow<ResponseVo<OrderVo>>

  /**
   * 取消订单
   */
  fun cancel(uid: Int, orderNo: Long): Flow<ResponseVo<Unit>>

  /**
   * 订单支付完成
   */
  fun paid(orderNo: Long): Flow<Unit>
}