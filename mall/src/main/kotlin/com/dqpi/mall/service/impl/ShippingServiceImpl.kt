package com.dqpi.mall.service.impl

import com.dqpi.mall.consts.ResponseEnum
import com.dqpi.mall.entity.Shipping
import com.dqpi.mall.repository.ShippingRepository
import com.dqpi.mall.service.ShippingService
import com.dqpi.mall.utils.toPageVo
import com.dqpi.mall.utils.toResponseVo
import com.dqpi.mall.utils.toShipping
import com.dqpi.mall.vo.PageVo
import com.dqpi.mall.vo.ResponseVo
import com.dqpi.mall.vo.ShippingForm
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * @author TheBIGMountain
 */
@Service
class ShippingServiceImpl(
  @Value("#{@shippingRepository}")
  private val shippingRepository: ShippingRepository
): ShippingService {
  /**
   * 添加地址
   */
  override fun add(uid: Int, form: ShippingForm): Flow<ResponseVo<Map<String, Int>>> {
    return shippingRepository.save(form.toShipping(uid)).asFlow()
      .map { mapOf("shipping" to it.id!!).toResponseVo() }
  }

  /**
   * 删除地址
   */
  override fun delete(uid: Int, shippingId: Int): Flow<ResponseVo<Unit>> {
    return shippingRepository.deleteByIdIsAndUserIdIs(shippingId, uid).asFlow()
      .map { ResponseEnum.SUCCESS.toResponseVo() }
  }

  /**
   * 更新地址
   */
  override fun update(uid: Int, shippingId: Int, form: ShippingForm): Flow<ResponseVo<Unit>> {
    return shippingRepository.save(form.toShipping(uid).apply { id = shippingId }).asFlow()
      .map { ResponseEnum.SUCCESS.toResponseVo() }
  }

  /**
   * 分页获取地址列表
   */
  override fun list(uid: Int, pageNum: Int, pageSize: Int): Flow<ResponseVo<PageVo<List<Shipping>>>> {
    return flowOf(shippingRepository.findAllByUserIdIs(uid).asFlow())
      // 丢弃不满足当前分页的数据
      .map { it.drop((pageNum - 1) * pageSize).take(pageSize).toList() }
      // 组装数据并返回
      .map { it.toPageVo(pageNum, pageSize).toResponseVo() }
  }
}


