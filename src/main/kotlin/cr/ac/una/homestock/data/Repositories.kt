package cr.ac.una.homestock.data

import cr.ac.una.homestock.domain.model.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

/* =========================
   CORE
   ========================= */

@Repository
interface UserRepository : JpaRepository<User, UUID>

@Repository
interface CategoryRepository : JpaRepository<Category, UUID>

@Repository
interface StoreRepository : JpaRepository<Store, UUID>

/* =========================
   PRODUCT + MOVEMENT + SHOPPING
   ========================= */

@Repository
interface ProductRepository : JpaRepository<Product, UUID> {
    fun findAllByUser_Id(userId: UUID, pageable: Pageable): Page<Product>

    fun findByUser_IdAndCategory_Id(
        userId: UUID,
        categoryId: UUID,
        pageable: Pageable
    ): Page<Product>
}

@Repository
interface MovementRepository : JpaRepository<Movement, UUID> {
    fun findByProduct_IdOrderByOccurredAtDesc(productId: UUID): List<Movement>
}

@Repository
interface ShoppingItemRepository : JpaRepository<ShoppingItem, UUID> {
    @Query(
        "select s from ShoppingItem s " +
                "where s.product.id = :productId and s.purchasedAt is null"
    )
    fun findActiveByProductId(@Param("productId") productId: UUID): ShoppingItem?
}

/* =========================
   EXTRAS
   ========================= */
@Repository
interface AlertRepository : JpaRepository<Alert, UUID>

@Repository
interface PriceHistoryRepository : JpaRepository<PriceHistory, UUID>

@Repository
interface ProductRatingRepository : JpaRepository<ProductRating, UUID>
