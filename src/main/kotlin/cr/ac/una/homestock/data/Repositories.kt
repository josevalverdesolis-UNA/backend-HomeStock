package cr.ac.una.homestock.repository

import cr.ac.una.homestock.domain.entity.*
import org.springframework.data.jpa.repository.JpaRepository
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
    fun findAllByUser_IdAndIsPurchasedFalse(userId: Long): List<ShoppingItem>
    fun findByUser_IdAndProduct_IdAndIsPurchasedFalse(userId: Long, productId: Long): Optional<ShoppingItem>
    fun existsByUser_IdAndProduct_IdAndIsPurchasedFalse(userId: Long, productId: Long): Boolean
}

// ------------------------------
// Alert
// ------------------------------

@Repository
interface AlertRepository : JpaRepository<Alert, Long> {
    fun findAllByUser_IdAndIsActiveTrue(userId: Long): List<Alert>
    fun findAllByUser_IdAndProduct_IdAndIsActiveTrue(userId: Long, productId: Long): List<Alert>
    fun findAllByTriggerAtBeforeAndIsActiveTrue(now: Instant): List<Alert>
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

// Comentario de cambios: StoreRepository agrega existsByNameIgnoreCaseAndLocation* para UQ compuesta
