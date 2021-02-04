package com.dqpi.mall.service

import com.dqpi.mall.vo.PageVo
import com.dqpi.mall.vo.ProductDetailVo
import com.dqpi.mall.vo.ProductVo
import com.dqpi.mall.vo.ResponseVo
import kotlinx.coroutines.flow.Flow

/**
 * @author TheBIGMountain
 */
interface ProductService {

  /**
   * 查询所有当前商品及子商品, 若不存在, 则查询所有
   */
  fun list(categoryId: Int, pageNum: Int, pageSize: Int): Flow<ResponseVo<PageVo<List<ProductVo>>>>

  /**
   * 根据id查询商品详情
   */
  fun detail(productId: Int): Flow<ResponseVo<ProductDetailVo>>
}