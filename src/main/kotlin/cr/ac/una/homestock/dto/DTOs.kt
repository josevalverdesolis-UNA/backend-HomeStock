package cr.ac.una.homestock.dto

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.util.*

/* Estilo:
   - *Input*: campos nullable para soportar partial update (PATCH).
   - *Result*: sin detalles internos ni perezosos.
   - Fechas con java.util.Date (simple, sin zona). */

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserInput(
    @field:Size(max = 64) val id: String? = null,
    @field:Size(max = 120) val name: String? = null,
    @field:Email @field:Size(max = 160) val email: String? = null,
)
data class UserResult(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: Date,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CategoryInput(
    @field:Size(max = 80) val name: String? = null,
)
data class CategoryResult(
    val id: String,
    val name: String,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class StoreInput(
    @field:Size(max = 120) val name: String? = null,
    @field:Size(max = 200) val address: String? = null,
    @field:Size(max = 120) val district: String? = null,
    @field:Size(max = 120) val city: String? = null,
)
data class StoreResult(
    val id: String,
    val name: String,
    val address: String?,
    val district: String?,
    val city: String?,
    val createdAt: Date,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductInput(
    @field:Size(max = 64)  val userId: String? = null,
    @field:Size(max = 64)  val categoryId: String? = null,
    @field:Size(max = 120) val name: String? = null,
    @field:Size(max = 80)  val brand: String? = null,
    val quantity: Int? = null,
    val minStock: Int? = null,
    val acquisitionDate: Date? = null,
    val expiryDate: Date? = null,
    @field:PositiveOrZero val price: BigDecimal? = null,
    @field:Size(max = 64)  val purchaseLocationId: String? = null,
    @field:Size(max = 512) val imageUrl: String? = null,
)
data class ProductResult(
    val id: String,
    val userId: String,
    val category: CategoryResult,
    val name: String,
    val brand: String?,
    val quantity: Int,
    val minStock: Int,
    val acquisitionDate: Date?,
    val expiryDate: Date?,
    val price: BigDecimal?,
    val purchaseLocationId: String?,
    val imageUrl: String?,
    val createdAt: Date,
    val updatedAt: Date,
)

enum class MovementType { PURCHASE, CONSUMPTION, ADJUSTMENT }

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MovementInput(
    @field:Size(max = 64) val productId: String? = null,
    @field:Size(max = 64) val userId: String? = null,
    @field:Size(max = 64) val storeId: String? = null,
    val type: MovementType? = null,
    val quantity: Int? = null,
    @field:PositiveOrZero val unitPrice: BigDecimal? = null,
    @field:Size(max = 200) val note: String? = null,
    val occurredAt: Date? = null,
)
data class MovementResult(
    val id: String,
    val productId: String,
    val userId: String,
    val storeId: String?,
    val type: MovementType,
    val quantity: Int,
    val unitPrice: BigDecimal?,
    val note: String?,
    val occurredAt: Date,
)

enum class ShoppingSource { AUTO_RULE, MANUAL }

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ShoppingItemInput(
    @field:Size(max = 64) val userId: String? = null,
    @field:Size(max = 64) val productId: String? = null,
    val desiredQuantity: Int? = null,
    val isPurchased: Boolean? = null,
    @field:Size(max = 64) val targetStoreId: String? = null,
    val source: ShoppingSource? = null,
    val purchasedAt: Date? = null,
)
data class ShoppingItemResult(
    val id: String,
    val userId: String,
    val productId: String,
    val desiredQuantity: Int,
    val isPurchased: Boolean,
    val targetStoreId: String?,
    val source: ShoppingSource,
    val createdAt: Date,
    val purchasedAt: Date?,
)

enum class AlertType { EXPIRY, LOW_STOCK }

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AlertInput(
    @field:Size(max = 64) val userId: String? = null,
    @field:Size(max = 64) val productId: String? = null,
    val type: AlertType? = null,
    val triggerAt: Date? = null,
    val isActive: Boolean? = null,
    val resolvedAt: Date? = null,
)
data class AlertResult(
    val id: String,
    val userId: String,
    val productId: String,
    val type: AlertType,
    val triggerAt: Date,
    val isActive: Boolean,
    val resolvedAt: Date?,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PriceHistoryInput(
    @field:Size(max = 64) val productId: String? = null,
    @field:Size(max = 64) val storeId: String? = null,
    @field:PositiveOrZero val unitPrice: BigDecimal? = null,
    val recordedAt: Date? = null,
)
data class PriceHistoryResult(
    val id: String,
    val productId: String,
    val storeId: String,
    val unitPrice: BigDecimal,
    val recordedAt: Date,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductRatingInput(
    @field:Size(max = 64) val userId: String? = null,
    @field:Size(max = 64) val productId: String? = null,
    @field:Min(1) @field:Max(5) val qualityScore: Int? = null,
    @field:Size(max = 300) val notes: String? = null,
    val createdAt: Date? = null,
)
data class ProductRatingResult(
    val id: String,
    val userId: String,
    val productId: String,
    val qualityScore: Int,
    val notes: String?,
    val createdAt: Date,
)
