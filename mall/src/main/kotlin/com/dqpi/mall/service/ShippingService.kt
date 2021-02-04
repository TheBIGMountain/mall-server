package com.dqpi.mall.service

import com.dqpi.mall.entity.Shipping
import com.dqpi.mall.vo.PageVo
import com.dqpi.mall.vo.ResponseVo
import com.dqpi.mall.vo.ShippingForm
import kotlinx.coroutines.flow.Flow

/**
 * @author TheBIGMountain
 */
interface ShippingService {
  /**
   * 添加地址
   */
  fun add(uid: Int, form: ShippingForm): Flow<ResponseVo<Map<String, Int>>>

  /**
   * 删除地址
   */
  fun delete(uid: Int, shippingId: Int): Flow<ResponseVo<Unit>>

  /**
   * 更新地址
   */
  fun update(uid: Int, shippingId: Int, form: ShippingForm): Flow<ResponseVo<Unit>>

  /**
   * 分页获取地址列表
   */
  fun list(uid: Int, pageNum: Int, pageSize: Int): Flow<ResponseVo<PageVo<List<Shipping>>>>
}