package cr.una.homestock.data.repository

import cr.una.homestock.domain.model.*
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, String> {
    fun findByEmail(email: String): User?
}

interface CategoryRepository : JpaRepository<Category, String> {
    fun findByName(name: String): Category?
}

interface StoreRepository : JpaRepository<Store, String>

interface ProductRepository : JpaRepository<Product, String> {
    fun findByUserId(userId: String): List<Product>
    fun existsByUserIdAndId(userId: String, id: String): Boolean
}

interface MovementRepository : JpaRepository<Movement, String> {
    fun findByProductIdOrderByOccurredAtDesc(productId: String): List<Movement>
}

interface ShoppingItemRepository : JpaRepository<ShoppingItem, String> {
    fun findByUserIdAndIsPurchased(userId: String, isPurchased: Boolean): List<ShoppingItem>
    fun existsByProductIdAndIsPurchased(productId: String, isPurchased: Boolean): Boolean
}

interface AlertRepository : JpaRepository<Alert, String> {
    fun findByUserIdAndIsActive(userId: String, isActive: Boolean): List<Alert>
}

interface PriceHistoryRepository : JpaRepository<PriceHistory, String> {
    fun findByProductIdAndStoreIdOrderByRecordedAtDesc(productId: String, storeId: String): List<PriceHistory>
}

interface ProductRatingRepository : JpaRepository<ProductRating, String> {
    fun findByUserIdAndProductId(userId: String, productId: String): ProductRating?
}
