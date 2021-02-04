package com.dqpi.mall.service.impl

import com.dqpi.mall.consts.ResponseEnum
import com.dqpi.mall.exceptions.BusinessException
import com.dqpi.mall.repository.ProductRepository
import com.dqpi.mall.service.CategoryService
import com.dqpi.mall.service.ProductService
import com.dqpi.mall.utils.toPageVo
import com.dqpi.mall.utils.toProductDetailVo
import com.dqpi.mall.utils.toProductVo
import com.dqpi.mall.utils.toResponseVo
import com.dqpi.mall.vo.PageVo
import com.dqpi.mall.vo.ProductDetailVo
import com.dqpi.mall.vo.ProductVo
import com.dqpi.mall.vo.ResponseVo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

/**
 * @author TheBIGMountain
 */
@Service
class ProductServiceImpl(
  @Value("#{@categoryServiceImpl}")
  private val categoryService: CategoryService,
  @Value("#{@productRepository}")
  private val productRepository: ProductRepository
): ProductService {
  /**
   * 查询所有当前商品及子商品, 若不存在, 则查询所有
   */
  override fun list(categoryId: Int, pageNum: Int, pageSize: Int): Flow<ResponseVo<PageVo<List<ProductVo>>>> {
    // 分页数据
    val pageRequest = PageRequest.of(pageNum - 1, pageSize)
    // 根据categoryId查询其子id集合
    return categoryService.findSubCategoryIds(categoryId)
      // 添加自身到id集合
      .map { it.toMutableSet().apply { add(categoryId) } }
      // 查询商品
      .map { productRepository.findAllByCategoryIdInAndStatusIs(it, pageable = pageRequest) }
      // 上面流程出现异常时则查询所有商品
      .catch { emit(productRepository.findAllByStatusIs(pageable = pageRequest)) }
      // 转换对象 product -> productVo
      .map { products -> products.asFlow().map { it.toProductVo() } }
      // 返回结果
      .map { it.toList().toPageVo(pageNum, pageSize).toResponseVo() }
  }

  /**
   * 根据id查询商品详情
   */
  override fun detail(productId: Int): Flow<ResponseVo<ProductDetailVo>> {
    // 查询数据库中正在上架的商品
    return productRepository.findByIdIsAndStatusIs(productId).asFlow()
      // 不存在则抛出
      .onEmpty { throw BusinessException(ResponseEnum.PRODUCT_OFF_SALE_OR_DELETE) }
      // 转换对象, 并返回结果
      .map { it.toProductDetailVo().toResponseVo() }
  }
}
