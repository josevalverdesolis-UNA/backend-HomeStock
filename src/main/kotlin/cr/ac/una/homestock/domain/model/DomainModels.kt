package cr.ac.una.homestock.domain.model


import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/* ============================================================================
 * Domain (modelo puro, sin JPA ni frameworks)
 *  - Estos modelos reflejan el negocio y no dependen de persistencia.
 *  - Se mapean desde/hacia DTOs y Entities (JPA) en la capa mapper.
 * ========================================================================== */

/* ----- Enums de dominio (comparten semántica con la capa data) ----- */
enum class MovementType { PURCHASE, CONSUMPTION, ADJUSTMENT }
enum class ShoppingSource { AUTO_RULE, MANUAL }
enum class AlertType { EXPIRY, LOW_STOCK }

/* ----- Núcleo ----- */
data class User(
    val id: String,                 // UID externo (Firebase)
    val name: String,
    val email: String,
    val createdAt: Instant
)

data class Category(
    val id: UUID,
    val name: String
)

data class Store(
    val id: UUID,
    val name: String,
    val address: String? = null,
    val district: String? = null,
    val city: String? = null,
    val createdAt: Instant
)

/**
 * Agregado principal de inventario.
 * Reglas típicas:
 *  - quantity >= 0
 *  - minStock >= 0
 *  - expiryDate opcional
 */
data class Product(
    val id: UUID,
    val userId: String,
    val categoryId: UUID,
    val name: String,
    val brand: String? = null,
    val quantity: Int,
    val minStock: Int,
    val acquisitionDate: LocalDate? = null,
    val expiryDate: LocalDate? = null,
    val price: BigDecimal? = null,
    val purchaseLocationId: UUID? = null,
    val imageUrl: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(quantity >= 0) { "quantity must be >= 0" }
        require(minStock >= 0) { "minStock must be >= 0" }
    }

    fun needsRestock(): Boolean = quantity <= minStock
}

/* ----- Historial / Movimientos ----- */
/**
 * Movimiento de stock:
 *  - PURCHASE: quantity > 0, unitPrice requerido para análisis de costo
 *  - CONSUMPTION: quantity < 0
 *  - ADJUSTMENT: libre (+/-) para corregir inventario
 */
data class Movement(
    val id: UUID,
    val productId: UUID,
    val userId: String,
    val storeId: UUID?,
    val type: MovementType,
    val quantity: Int,
    val unitPrice: BigDecimal?,   // requerido lógicamente cuando type==PURCHASE
    val note: String?,
    val occurredAt: Instant
)

/* ----- Lista de compras ----- */
data class ShoppingItem(
    val id: UUID,
    val userId: String,
    val productId: UUID,
    val desiredQuantity: Int,
    val isPurchased: Boolean,
    val source: ShoppingSource,
    val targetStoreId: UUID?,
    val createdAt: Instant,
    val purchasedAt: Instant?
) {
    init { require(desiredQuantity > 0) { "desiredQuantity must be > 0" } }
}

/* ----- Alertas ----- */
data class Alert(
    val id: UUID,
    val userId: String,
    val productId: UUID,
    val type: AlertType,
    val triggerAt: Instant,
    val isActive: Boolean,
    val resolvedAt: Instant?
)

/* ----- Historial de precios ----- */
data class PriceHistory(
    val id: UUID,
    val productId: UUID,
    val storeId: UUID,
    val unitPrice: BigDecimal,
    val recordedAt: Instant
)

/* ----- Calificaciones ----- */
data class ProductRating(
    val id: UUID,
    val userId: String,
    val productId: UUID,
    val qualityScore: Int,   // 1..5
    val notes: String?,
    val createdAt: Instant
) {
    init { require(qualityScore in 1..5) { "qualityScore must be in 1..5" } }
}
