package com.dqpi.mall.service.impl

import com.dqpi.mall.consts.MallConst
import com.dqpi.mall.repository.CategoryRepository
import com.dqpi.mall.service.CategoryService
import com.dqpi.mall.utils.toCategoryVo
import com.dqpi.mall.utils.toResponseVo
import com.dqpi.mall.vo.CategoryVo
import com.dqpi.mall.vo.ResponseVo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

/**
 * @author TheBIGMountain
 */
@Service
class CategoryServiceImpl(
  @Value("#{categoryRepository}")
  private val categoryRepository: CategoryRepository
): CategoryService{
  /**
   * 查询所有类目
   *
   * @return 所有类目
   */
  override fun findAll(): Flow<ResponseVo<Collection<CategoryVo>>> {
    return flowOf(TreeSet<CategoryVo>())
      // 存储根目录的结果集合
      .onEach { result ->
        categoryRepository.findAllByStatusIs().asFlow()
          // 转换类型
          .map { it.toCategoryVo() }
          // 添加根目录到结果集
          .onEach { if (it.parentId == MallConst.ROOT_CATEGORY) result.add(it) }
          // 收集数据
          .toList().let { list ->
            // 映射所有的条目
            val map = list.associateBy { categoryVo -> categoryVo.id }
            // 逐个添加到子目录中
            list.forEach { map[it.parentId]?.subCategories?.add(it) }
          }
        // 转换结果
      }.map { it.toResponseVo() }
  }

  /**
   * 查询所有子类目的id
   *
   * @param id 父类目id
   * @return 子类目id集合
   */
  override fun findSubCategoryIds(id: Int): Flow<Set<Int>> {
    // 获取数据库数据
    return findAll().map { res -> res.data!!.associateBy { it.id } }
      // 获取查询id目录的子目录
      .map { it[id]?.subCategories ?: throw RuntimeException("不存在的id") }
      // 转换子类目id集合
      .map { HashSet<Int>().apply { findSubCategoryIds(it) } }
  }

  /**
   * 递归添加子类目id
   */
  private fun MutableSet<Int>.findSubCategoryIds(categorySet: Set<CategoryVo>) {
    categorySet.forEach {
      add(it.id!!)
      findSubCategoryIds(it.subCategories)
    }
  }
}