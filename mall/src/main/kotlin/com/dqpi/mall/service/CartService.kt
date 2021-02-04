package com.dqpi.mall.service

import com.dqpi.mall.entity.Cart
import com.dqpi.mall.vo.CartAddForm
import com.dqpi.mall.vo.CartUpdateForm
import com.dqpi.mall.vo.CartVo
import com.dqpi.mall.vo.ResponseVo
import kotlinx.coroutines.flow.Flow

/**
 * @author TheBIGMountain
 */
interface CartService {

  /**
   * 购物车添加商品
   *
   * @param form 商品信息
   */
  fun add(uid: Int, form: CartAddForm): Flow<ResponseVo<CartVo>>

  /**
   * 获取购物车列表
   *
   * @param uid 用户id
   */
  fun list(uid: Int): Flow<ResponseVo<CartVo>>


  /**
   * 更新购物车列表
   */
  fun update(uid: Int, productId: Int, form: CartUpdateForm): Flow<ResponseVo<CartVo>>


  /**
   * 删除指定购物车中的商品
   */
  fun delete(uid: Int, productId: Int): Flow<ResponseVo<CartVo>>

  /**
   * 全选
   */
  fun selectAll(uid: Int): Flow<ResponseVo<CartVo>>

  /**
   * 全设为非选中
   */
  fun unSelectAll(uid: Int): Flow<ResponseVo<CartVo>>

  /**
   * 商品数量总和
   */
  fun sum(uid: Int): Flow<ResponseVo<Int>>

  /**
   * 遍历购物车商品
   *
   * @param uid 用户id
   * @param ops 遍历过程需要执行的操作
   */
  fun foreachProduct(uid: Int, ops: (Pair<String, Cart>) -> Unit): Flow<Unit>
}