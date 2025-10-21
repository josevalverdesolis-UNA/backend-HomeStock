@file:Suppress("unused")

package cr.ac.una.homestock.repository

import cr.ac.una.homestock.domain.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

// ------------------------------
// User
// ------------------------------

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
}

// ------------------------------
// Category
// ------------------------------

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByNameIgnoreCase(name: String): Optional<Category>
    fun existsByNameIgnoreCase(name: String): Boolean
}

// ------------------------------
// Store
// ------------------------------

@Repository
interface StoreRepository : JpaRepository<Store, Long> {
    fun findByNameIgnoreCase(name: String): Optional<Store>
    fun existsByNameIgnoreCase(name: String): Boolean
    fun existsByNameIgnoreCaseAndLocationIgnoreCase(name: String, location: String): Boolean
    fun existsByNameIgnoreCaseAndLocationIsNull(name: String): Boolean
}

// ------------------------------
// Product
// ------------------------------

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findAllByUser_Id(userId: Long): List<Product>
    fun findAllByUser_IdAndCategory_Id(userId: Long, categoryId: Long): List<Product>
    fun findByUser_IdAndId(userId: Long, id: Long): Optional<Product>

    fun existsByUser_IdAndNameIgnoreCase(userId: Long, name: String): Boolean
    fun findAllByUser_IdAndNameContainingIgnoreCase(userId: Long, name: String): List<Product>

    @Query(
        """
        SELECT p FROM Product p
        WHERE p.user.id = :userId
          AND (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(COALESCE(p.brand, '')) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(COALESCE(p.barcode, '')) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:minStockOnly = FALSE OR p.quantity <= p.minStock)
        """
    )
    fun search(
        @Param("userId") userId: Long,
        @Param("q") q: String?,
        @Param("categoryId") categoryId: Long?,
        @Param("minStockOnly") minStockOnly: Boolean,
        pageable: Pageable
    ): Page<Product>

    fun findFirstByUser_IdAndBarcode(userId: Long, barcode: String): Optional<Product>
}

// ------------------------------
// Movement
// ------------------------------

@Repository
interface MovementRepository : JpaRepository<Movement, Long> {
    fun findAllByUser_Id(userId: Long): List<Movement>
    fun findAllByProduct_IdOrderByCreatedAtDesc(productId: Long): List<Movement>
}

// ------------------------------
// ShoppingItem
// ------------------------------

@Repository
interface ShoppingItemRepository : JpaRepository<ShoppingItem, Long> {
    fun findAllByUser_IdAndPurchasedFalse(userId: Long): List<ShoppingItem>
    fun findByUser_IdAndProduct_IdAndPurchasedFalse(userId: Long, productId: Long): Optional<ShoppingItem>
    fun existsByUser_IdAndProduct_IdAndPurchasedFalse(userId: Long, productId: Long): Boolean
}

// ------------------------------
// Alert
// ------------------------------

@Repository
interface AlertRepository : JpaRepository<Alert, Long> {
    fun findAllByUser_IdAndActiveTrue(userId: Long): List<Alert>
    fun findAllByUser_IdAndProduct_IdAndActiveTrue(userId: Long, productId: Long): List<Alert>
    fun findAllByTriggerAtBeforeAndActiveTrue(now: Instant): List<Alert>
}

// ------------------------------
// PriceHistory
// ------------------------------

@Repository
interface PriceHistoryRepository : JpaRepository<PriceHistory, Long> {
    fun findTop1ByProduct_IdOrderByRecordedAtDesc(productId: Long): Optional<PriceHistory>
}

// ------------------------------
// ProductRating
// ------------------------------

@Repository
interface ProductRatingRepository : JpaRepository<ProductRating, Long> {
    fun findByUser_IdAndProduct_Id(userId: Long, productId: Long): Optional<ProductRating>
    fun findAllByProduct_Id(productId: Long): List<ProductRating>
}

// ------------------------------
// RefreshToken
// ------------------------------

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByTokenHash(tokenHash: String): Optional<RefreshToken>
    fun findAllByUser_IdAndRevokedAtIsNull(userId: Long): List<RefreshToken>
}

// Comentario de cambios: Actualizados métodos derivados para purchased/active
