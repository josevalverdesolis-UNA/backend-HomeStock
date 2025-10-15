package cr.ac.una.homestock.data

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<UserEntity, String> {
    fun findByEmailIgnoreCase(email: String): Optional<UserEntity>
    fun existsByEmailIgnoreCase(email: String): Boolean
}

interface CategoryRepository : JpaRepository<CategoryEntity, String> {
    fun existsByNameIgnoreCase(name: String): Boolean
}

interface StoreRepository : JpaRepository<StoreEntity, String>

interface ProductRepository : JpaRepository<ProductEntity, String> {
    fun findAllByUser_Id(userId: String): List<ProductEntity>
    fun findByIdAndUser_Id(id: String, userId: String): Optional<ProductEntity>
}

interface MovementRepository : JpaRepository<MovementEntity, String> {
    fun findAllByUser_Id(userId: String): List<MovementEntity>
    fun findAllByProduct_Id(productId: String): List<MovementEntity>
}

interface ShoppingItemRepository : JpaRepository<ShoppingItemEntity, String> {
    fun findAllByUser_Id(userId: String): List<ShoppingItemEntity>
    fun findAllByUser_IdAndIsPurchased(userId: String, isPurchased: Boolean): List<ShoppingItemEntity>
}

interface AlertRepository : JpaRepository<AlertEntity, String>

interface PriceHistoryRepository : JpaRepository<PriceHistoryEntity, String> {
    fun findAllByProduct_Id(productId: String): List<PriceHistoryEntity>
    fun findAllByStore_Id(storeId: String): List<PriceHistoryEntity>
}

interface ProductRatingRepository : JpaRepository<ProductRatingEntity, String> {
    fun findByUser_IdAndProduct_Id(userId: String, productId: String): Optional<ProductRatingEntity>
    fun existsByUser_IdAndProduct_Id(userId: String, productId: String): Boolean
}
