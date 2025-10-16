package cr.ac.una.homestock.dto

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import kotlin.reflect.KClass
import cr.ac.una.homestock.web.NotZeroValidator
import jakarta.validation.Constraint

/**
 * DTOs alineados con el ER de HomeStock.
 *
 * Convenciones:
 * - Create: todos los campos requeridos para crear.
 * - Update: campos opcionales (nullable) para soportar PATCH/PUT parcial.
 * - Result: representación de salida hacia la capa web.
 * - Enums: idénticos a lo definido en el modelo (PURCHASE|CONSUMPTION|ADJUSTMENT, etc.).
 */

// ------------------------------
// Enums públicos del contrato API
// ------------------------------

enum class MovementType { PURCHASE, CONSUMPTION, ADJUSTMENT }

enum class ShoppingSource { AUTO_RULE, MANUAL }

enum class AlertType { LOW_STOCK, EXPIRY }

// ------------------------------
// Infra DTOs (paginación, respuestas genéricas)
// ------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiMessage(val message: String)

// ------------------------------
// Category
// ------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CategoryCreate(
    @field:NotBlank
    val name: String,
    val description: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CategoryUpdate(
    val name: String? = null,
    val description: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CategoryResult(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

// ------------------------------
// Store (lugar de compra)
// ------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
data class StoreCreate(
    @field:NotBlank
    val name: String,
    val location: String? = null,
    val notes: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class StoreUpdate(
    val name: String? = null,
    val location: String? = null,
    val notes: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class StoreResult(
    val id: Long,
    val name: String,
    val location: String?,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

// ------------------------------
// Product
// ------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductCreate(
    @field:NotNull
    val userId: Long,
    @field:NotBlank
    val name: String,
    @field:NotNull
    val categoryId: Long,
    @field:Min(0)
    val quantity: Int = 0,
    @field:Min(0)
    val minStock: Int = 0,
    val expiryDate: LocalDate? = null,
    @field:Positive
    val price: BigDecimal? = null,
    val purchaseLocationId: Long? = null,
    val brand: String? = null,
    @field:Pattern(regexp = "^https?://.*", message = "imageUrl debe ser http(s)")
    val imageUrl: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductUpdate(
    val name: String? = null,
    val categoryId: Long? = null,
    @field:Min(0)
    val quantity: Int? = null,
    @field:Min(0)
    val minStock: Int? = null,
    val expiryDate: LocalDate? = null,
    @field:Positive
    val price: BigDecimal? = null,
    val purchaseLocationId: Long? = null,
    val brand: String? = null,
    @field:Pattern(regexp = "^https?://.*", message = "imageUrl debe ser http(s)")
    val imageUrl: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductResult(
    val id: Long,
    val userId: Long,
    val name: String,
    val categoryId: Long,
    val quantity: Int,
    val minStock: Int,
    val expiryDate: LocalDate?,
    val price: BigDecimal?,
    val purchaseLocationId: Long?,
    val brand: String?,
    val imageUrl: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

// ------------------------------
// Movement (impacta stock)
// ------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MovementCreate(
    @field:NotNull
    val userId: Long,
    @field:NotNull
    val productId: Long,
    @field:NotNull
    val type: MovementType,
    /**
     * Convención de signo a nivel de servicio:
     *  - PURCHASE      => quantity > 0
     *  - CONSUMPTION   => quantity < 0 (o se normaliza internamente)
     *  - ADJUSTMENT    => positivo o negativo
     */
    @field:NotZero(message = "quantity no puede ser 0")
    val quantity: Int,
    @field:Positive
    val unitPrice: BigDecimal? = null, // requerido cuando type=PURCHASE (validado en servicio)
    val storeId: Long? = null,
    val note: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MovementResult(
    val id: Long,
    val userId: Long,
    val productId: Long,
    val type: MovementType,
    val quantity: Int,
    val unitPrice: BigDecimal?,
    val storeId: Long?,
    val note: String?,
    val createdAt: Instant,
)

// ------------------------------
// ShoppingItem (lista de compras)
// ------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ShoppingItemCreate(
    @field:NotNull
    val userId: Long,
    @field:NotNull
    val productId: Long,
    @field:Min(1)
    val quantity: Int = 1,
    val source: ShoppingSource = ShoppingSource.MANUAL,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ShoppingItemUpdate(
    @field:Min(1)
    val quantity: Int? = null,
    val isPurchased: Boolean? = null,
    val purchasedAt: Instant? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ShoppingItemResult(
    val id: Long,
    val userId: Long,
    val productId: Long,
    val quantity: Int,
    val isPurchased: Boolean,
    val purchasedAt: Instant?,
    val source: ShoppingSource,
    val createdAt: Instant,
)

// ------------------------------
// Alert
// ------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AlertCreate(
    @field:NotNull
    val userId: Long,
    val productId: Long? = null, // puede ser null para alertas generales
    @field:NotNull
    val type: AlertType,
    val message: String? = null,
    val triggerAt: Instant? = null, // si null, se calculará en servicio
    val isActive: Boolean = true,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AlertUpdate(
    val message: String? = null,
    val triggerAt: Instant? = null,
    val isActive: Boolean? = null,
    val resolvedAt: Instant? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AlertResult(
    val id: Long,
    val userId: Long,
    val productId: Long?,
    val type: AlertType,
    val message: String?,
    val triggerAt: Instant,
    val isActive: Boolean,
    val resolvedAt: Instant?,
    val createdAt: Instant,
)

// ------------------------------
// PriceHistory (histórico de precios)
// ------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PriceHistoryCreate(
    @field:NotNull
    val productId: Long,
    @field:Positive
    val unitPrice: BigDecimal,
    val storeId: Long? = null,
    val recordedAt: Instant? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PriceHistoryResult(
    val id: Long,
    val productId: Long,
    val unitPrice: BigDecimal,
    val storeId: Long?,
    val recordedAt: Instant,
)

// ------------------------------
// ProductRating (valoración de producto)
// ------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductRatingCreate(
    @field:NotNull
    val userId: Long,
    @field:NotNull
    val productId: Long,
    @field:Min(1)
    @field:Max(5)
    val qualityScore: Int,
    val notes: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductRatingResult(
    val id: Long,
    val userId: Long,
    val productId: Long,
    val qualityScore: Int,
    val notes: String?,
    val createdAt: Instant,
)

// ------------------------------
// Validaciones adicionales
// ------------------------------

/** NotZero custom composed constraint for Ints */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [NotZeroValidator::class])
annotation class NotZero(
    val message: String = "must not be zero",
    val groups: Array<KClass<*>> = emptyArray(),
    val payload: Array<KClass<out Any>> = emptyArray()
)

// Nota: La validación real de @NotZero para Int puede implementarse con un ConstraintValidator en otra clase si se requiere.
