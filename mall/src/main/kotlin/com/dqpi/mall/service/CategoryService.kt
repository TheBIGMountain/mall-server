package com.dqpi.mall.service

import com.dqpi.mall.vo.CategoryVo
import com.dqpi.mall.vo.ResponseVo
import kotlinx.coroutines.flow.Flow

/**
 * @author TheBIGMountain
 */
interface CategoryService {

  /**
   * 查询所有类目
   *
   * @return 所有类目
   */
  fun findAll(): Flow<ResponseVo<Collection<CategoryVo>>>

  /**
   * 查询所有子类目的id
   *
   * @param id 父类目id
   * @return 子类目id集合
   */
  fun findSubCategoryIds(id: Int): Flow<Set<Int>>
}