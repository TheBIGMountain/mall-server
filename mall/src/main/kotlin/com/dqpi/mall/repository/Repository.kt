package com.dqpi.mall.repository

import com.dqpi.mall.consts.ProductEnum.ON_SALE
import com.dqpi.mall.entity.*
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


interface CategoryRepository: ReactiveCrudRepository<Category, Int> {
  fun findAllByStatusIs(status: Int = ON_SALE.code): Flux<Category>
}

interface ProductRepository: ReactiveCrudRepository<Product, Int> {
  fun findAllByStatusIs(status: Int = ON_SALE.code, pageable: Pageable): Flux<Product>
  fun findByIdIsAndStatusIs(id: Int, status: Int = ON_SALE.code): Mono<Product>
  fun findAllByCategoryIdInAndStatusIs(set: Set<Int>, status: Int = ON_SALE.code, pageable: Pageable): Flux<Product>
}

interface ShippingRepository: ReactiveCrudRepository<Shipping, Int> {
  fun deleteByIdIsAndUserIdIs(id: Int, userId: Int): Mono<Int>
  fun findAllByUserIdIs(userId: Int): Flux<Shipping>
  fun findByIdIsAndUserIdIs(id: Int, userId: Int): Mono<Shipping>
}

interface UserRepository: ReactiveCrudRepository<User, Int> {
  fun countByUsernameIs(userName: String): Mono<Int>
  fun countByEmailIs(email: String): Mono<Int>
  fun findByUsernameIs(username: String): Mono<User>
}

interface OrderRepository: ReactiveCrudRepository<Order, Int> {
  fun findAllByUserIdIs(userId: Int): Flux<Order>
  fun findByOrderNoIs(orderNo: Long): Flux<Order>
}

interface OrderItemRepository: ReactiveCrudRepository<OrderItem, Int> {
  fun findByOrderNoIs(orderNo: Long): Flux<OrderItem>
}

