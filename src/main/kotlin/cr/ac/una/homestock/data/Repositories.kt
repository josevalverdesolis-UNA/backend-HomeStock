package cr.una.homestock.repository

import cr.una.homestock.domain.model.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/* =========================
   CORE
   ========================= */

@Repository
interface UserRepository : JpaRepository<User, String>

@Repository
interface CategoryRepository : JpaRepository<Category, String>

@Repository
interface StoreRepository : JpaRepository<Store, String>

/* =========================
   PRODUCT + MOVEMENT + SHOPPING
   ========================= */

@Repository
interface ProductRepository : JpaRepository<Product, String> {
    fun findAllByUserId(userId: String, pageable: Pageable): Page<Product>

    fun findByUserIdAndCategoryId(
        userId: String,
        categoryId: String,
        pageable: Pageable
    ): Page<Product>
}

@Repository
interface MovementRepository : JpaRepository<Movement, String> {
    fun findByProductIdOrderByOccurredAtDesc(productId: String): List<Movement>
}

@Repository
interface ShoppingItemRepository : JpaRepository<ShoppingItem, String> {
    @Query(
        "select s from ShoppingItem s " +
                "where s.product.id = :productId and s.isPurchased = false"
    )
    fun findActiveByProductId(@Param("productId") productId: String): ShoppingItem?
}

/* =========================
   EXTRAS
   ========================= */
@Repository
interface AlertRepository : JpaRepository<Alert, String>

@Repository
interface PriceHistoryRepository : JpaRepository<PriceHistory, String>

@Repository
interface ProductRatingRepository : JpaRepository<ProductRating, String>
