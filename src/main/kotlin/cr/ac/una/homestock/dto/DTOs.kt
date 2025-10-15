package cr.una.homestock.web.dto

import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

/* ======= ENUM DTOs ======= */
enum class MovementTypeDto { PURCHASE, CONSUMPTION, ADJUSTMENT }
enum class ShoppingSourceDto { AUTO_RULE, MANUAL }
enum class AlertTypeDto { EXPIRY, LOW_STOCK }

/* ======= BASE ======= */
data class ApiResponse<T>(val data: T)
data class IdResponse(val id: String)

/* ======= USER ======= */
data class UserInput(
    @field:NotBlank val name: String?,
    @field:Email val email: String?
)
data class UserUpdate(
    val name: String?,
    @field:Email val email: String?
)
data class UserResult(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: OffsetDateTime
)

/* ======= CATEGORY ======= */
data class CategoryInput(@field:NotBlank val name: String?)
data class CategoryUpdate(val name: String?)
data class CategoryResult(val id: String, val name: String)

/* ======= STORE ======= */
data class StoreInput(
    @field:NotBlank val name: String?,
    val address: String?,
    val district: String?,
    val city: String?
)
data class StoreUpdate(
    val name: String?,
    val address: String?,
    val district: String?,
    val city: String?
)
data class StoreResult(
    val id: String,
    val name: String,
    val address: String?,
    val district: String?,
    val city: String?,
    val createdAt: OffsetDateTime
)

/* ======= PRODUCT ======= */
data class ProductInput(
    @field:NotBlank val userId: String?,
    @field:NotBlank val categoryId: String?,
    @field:NotBlank val name: String?,
    val brand: String?,
    @field:Min(0) val quantity: Int? = 0,
    @field:Min(0) val minStock: Int? = 0,
    val acquisitionDate: LocalDate?,
    val expiryDate: LocalDate?,
    @field:DecimalMin("0.0") val price: BigDecimal?,
    val purchaseLocationId: String?,
    val imageUrl: String?
)
data class ProductUpdate(
    val categoryId: String?,
    val name: String?,
    val brand: String?,
    @field:Min(0) val quantity: Int?,
    @field:Min(0) val minStock: Int?,
    val acquisitionDate: LocalDate?,
    val expiryDate: LocalDate?,
    @field:DecimalMin("0.0") val price: BigDecimal?,
    val purchaseLocationId: String?,
    val imageUrl: String?
)
data class ProductResult(
    val id: String,
    val userId: String,
    val categoryId: String,
    val name: String,
    val brand: String?,
    val quantity: Int,
    val minStock: Int,
    val acquisitionDate: LocalDate?,
    val expiryDate: LocalDate?,
    val price: BigDecimal?,
    val purchaseLocationId: String?,
    val imageUrl: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

/* ======= MOVEMENT ======= */
data class MovementInput(
    @field:NotBlank val productId: String?,
    @field:NotBlank val userId: String?,
    val storeId: String?,
    @field:NotNull val type: MovementTypeDto?,
    @field:NotNull val quantity: Int?, // >0 compra/ajuste+, <0 consumo/ajuste-
    @field:DecimalMin("0.0") val unitPrice: BigDecimal? = null,
    val note: String?,
    val occurredAt: OffsetDateTime? = null
)
data class MovementResult(
    val id: String,
    val productId: String,
    val userId: String,
    val storeId: String?,
    val type: MovementTypeDto,
    val quantity: Int,
    val unitPrice: BigDecimal?,
    val note: String?,
    val occurredAt: OffsetDateTime
)

/* ======= SHOPPING ITEM ======= */
data class ShoppingItemInput(
    @field:NotBlank val userId: String?,
    @field:NotBlank val productId: String?,
    @field:Min(1) val desiredQuantity: Int? = 1,
    val source: ShoppingSourceDto? = ShoppingSourceDto.MANUAL,
    val targetStoreId: String?
)
data class ShoppingItemUpdate(
    @field:Min(1) val desiredQuantity: Int?,
    val isPurchased: Boolean?,
    val targetStoreId: String?
)
data class ShoppingItemResult(
    val id: String,
    val userId: String,
    val productId: String,
    val desiredQuantity: Int,
    val isPurchased: Boolean,
    val source: ShoppingSourceDto,
    val targetStoreId: String?,
    val createdAt: OffsetDateTime,
    val purchasedAt: OffsetDateTime?
)

/* ======= ALERT ======= */
data class AlertInput(
    @field:NotBlank val userId: String?,
    @field:NotBlank val productId: String?,
    @field:NotNull val type: AlertTypeDto?,
    val triggerAt: OffsetDateTime?
)
data class AlertUpdate(
    val isActive: Boolean?,
    val resolvedAt: OffsetDateTime?
)
data class AlertResult(
    val id: String,
    val userId: String,
    val productId: String,
    val type: AlertTypeDto,
    val triggerAt: OffsetDateTime,
    val isActive: Boolean,
    val resolvedAt: OffsetDateTime?
)

/* ======= PRICE HISTORY ======= */
data class PriceHistoryInput(
    @field:NotBlank val productId: String?,
    @field:NotBlank val storeId: String?,
    @field:DecimalMin("0.0") val unitPrice: BigDecimal?,
    val recordedAt: OffsetDateTime? = null
)
data class PriceHistoryResult(
    val id: String,
    val productId: String,
    val storeId: String,
    val unitPrice: BigDecimal,
    val recordedAt: OffsetDateTime
)

/* ======= PRODUCT RATING ======= */
data class ProductRatingInput(
    @field:NotBlank val userId: String?,
    @field:NotBlank val productId: String?,
    @field:Min(1) @field:Max(5) val qualityScore: Int?,
    val notes: String?
)
data class ProductRatingUpdate(
    @field:Min(1) @field:Max(5) val qualityScore: Int?,
    val notes: String?
)
data class ProductRatingResult(
    val id: String,
    val userId: String,
    val productId: String,
    val qualityScore: Int,
    val notes: String?,
    val createdAt: OffsetDateTime
)
