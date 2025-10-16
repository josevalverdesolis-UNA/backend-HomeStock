package cr.ac.una.homestock.dto

import cr.ac.una.homestock.domain.model.AlertType
import cr.ac.una.homestock.domain.model.MovementType
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

/* =========================
 * Inputs
 * ========================= */
data class CategoryInput(
    @field:NotNull val userId: UUID?,
    @field:NotBlank val name: String,
    val description: String? = null
)

data class StoreInput(
    @field:NotNull val userId: UUID?,
    @field:NotBlank val name: String,
    val address: String? = null
)

data class ProductInput(
    @field:NotNull val userId: UUID?,
    @field:NotNull val categoryId: UUID?,
    @field:NotBlank val name: String,
    val brand: String? = null,
    val imageUrl: String? = null,
    /** quantity se ignora en create si manejas stock solo por movimientos */
    @field:Min(0) val minStock: Int = 1
)

data class ProductUpdateInput(
    val categoryId: UUID? = null,
    val name: String? = null,
    val brand: String? = null,
    val imageUrl: String? = null,
    val minStock: Int? = null
)

data class MovementInput(
    @field:NotNull val productId: UUID?,
    @field:NotNull val type: MovementType?,
    @field:Positive val quantity: Int,
    /** Requerido cuando type=PURCHASE */
    @field:DecimalMin("0.0") val unitPrice: BigDecimal? = null,
    val storeId: UUID? = null,
    val occurredAt: OffsetDateTime? = null
)

data class ShoppingItemInput(
    @field:NotNull val userId: UUID?,
    @field:NotNull val productId: UUID?,
    @field:Positive val quantity: Int = 1,
    val targetStoreId: UUID? = null
)

data class ShoppingItemPurchaseInput(
    val purchasedAt: OffsetDateTime = OffsetDateTime.now()
)

data class AlertInput(
    @field:NotNull val userId: UUID?,
    @field:NotNull val type: AlertType?,
    val message: String? = null,
    val triggerAt: OffsetDateTime? = null
)

data class PriceHistoryInput(
    @field:NotNull val productId: UUID?,
    val storeId: UUID? = null,
    @field:DecimalMin("0.0") val price: BigDecimal
)

data class ProductRatingInput(
    @field:NotNull val userId: UUID?,
    @field:NotNull val productId: UUID?,
    @field:Min(1) @field:Max(5) val score: Int,
    val comment: String? = null
)

/* =========================
 * Results / Details
 * ========================= */
data class CategoryResult(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val description: String?
)

data class StoreResult(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val address: String?
)

data class ProductResult(
    val id: UUID,
    val userId: UUID,
    val categoryId: UUID,
    val name: String,
    val brand: String?,
    val imageUrl: String?,
    val quantity: Int,
    val minStock: Int,
    val version: Long
)

data class MovementResult(
    val id: UUID,
    val productId: UUID,
    val type: MovementType,
    val quantity: Int,
    val unitPrice: BigDecimal?,
    val storeId: UUID?,
    val occurredAt: OffsetDateTime
)

data class ShoppingItemResult(
    val id: UUID,
    val userId: UUID,
    val productId: UUID,
    val quantity: Int,
    val targetStoreId: UUID?,
    val purchasedAt: OffsetDateTime?,
    val createdAt: OffsetDateTime,
    val source: String?
)

data class AlertResult(
    val id: UUID,
    val userId: UUID,
    val type: AlertType,
    val message: String?,
    val triggerAt: OffsetDateTime?,
    val isActive: Boolean,
    val resolvedAt: OffsetDateTime?
)

data class PriceHistoryResult(
    val id: UUID,
    val productId: UUID,
    val storeId: UUID?,
    val price: BigDecimal,
    val registeredAt: OffsetDateTime
)

data class ProductRatingResult(
    val id: UUID,
    val userId: UUID,
    val productId: UUID,
    val score: Int,
    val comment: String?
)
