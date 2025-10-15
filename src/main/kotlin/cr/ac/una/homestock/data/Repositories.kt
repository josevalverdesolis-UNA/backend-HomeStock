package cr.ac.una.homestock.data

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserJpaRepository : JpaRepository<UserEntity, String> {
    fun findByEmail(email: String): UserEntity?
}

interface CategoryJpaRepository : JpaRepository<CategoryEntity, UUID> {
    fun findByName(name: String): CategoryEntity?
    fun existsByNameIgnoreCase(name: String): Boolean
}

interface StoreJpaRepository : JpaRepository<StoreEntity, UUID>

interface ProductJpaRepository : JpaRepository<ProductEntity, UUID> {
    fun findByUser_Id(userId: String): List<ProductEntity>
    fun findByUser_IdAndCategory_Id(userId: String, categoryId: UUID): List<ProductEntity>
    fun findByNameContainingIgnoreCase(name: String): List<ProductEntity>
}

interface MovementJpaRepository : JpaRepository<MovementEntity, UUID>
interface ShoppingItemJpaRepository : JpaRepository<ShoppingItemEntity, UUID>
interface AlertJpaRepository : JpaRepository<AlertEntity, UUID>
interface PriceHistoryJpaRepository : JpaRepository<PriceHistoryEntity, UUID>
interface ProductRatingJpaRepository : JpaRepository<ProductRatingEntity, UUID>
