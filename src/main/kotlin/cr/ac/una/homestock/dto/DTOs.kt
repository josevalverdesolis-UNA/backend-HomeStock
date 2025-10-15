package cr.ac.una.homestock.dto


import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.*
import cr.ac.una.homestock.domain.model.MovementType
import cr.ac.una.homestock.domain.model.ShoppingSource
import cr.ac.una.homestock.domain.model.AlertType

// ============================================================================
// Revisión y criterios (resumen):
// - Frontend ya maneja Product con campos (name, category, quantity, acquisitionDate, price, brand,
//   purchaseLocation, imageUrl) y planea Purchase/ShoppingList/Alerts/PriceHistory.
// - Backend quedó con @Entity separadas (User, Category, Store, Product, Movement, ShoppingItem,
//   Alert, PriceHistory, ProductRating). IDs = UUID (menos User.id = String).
// - Para interoperar con Room/Compose y con un API REST limpia, los DTOs:
//   * Usan IDs planos (userId, productId, categoryId, storeId) en lugar de anidar entidades.
//   * Tienen variantes Create/Update con validaciones (Jakarta Validation).
//   * Devuelven DTOs “completos” con marcas de tiempo cuando aplica.
//   * Incluyen PageResponse opcional para paginación.
// ============================================================================

// -----------------------------
// Infra útil (opcional)
// -----------------------------
data class PageResponse<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int
)

// ============================================================================
// USER (referenciado por id:String en otros DTOs)
// ============================================================================
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: Instant
)

// ============================================================================
// CATEGORY
// ============================================================================
data class CategoryDto(
    val id: UUID,
    val name: String
)

data class CreateCategoryDto(
    @field:NotBlank val name: String
)

data class UpdateCategoryDto(
    val name: String?
)

// ============================================================================
// STORE
// ============================================================================
data class StoreDto(
    val id: UUID,
    val name: String,
    val address: String? = null,
    val district: String? = null,
    val city: String? = null,
    val createdAt: Instant
)

data class CreateStoreDto(
    @field:NotBlank val name: String,
    val address: String? = null,
    val district: String? = null,
    val city: String? = null
)

data class UpdateStoreDto(
    val name: String? = null,
    val address: String? = null,
    val district: String? = null,
    val city: String? = null
)

// ============================================================================
// PRODUCT
// ============================================================================
// Respuesta “completa” para UI y sincronización
data class ProductDto(
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
)

// Creación: FK obligatorias (userId, categoryId). Store opcional.
data class CreateProductDto(
    @field:NotBlank val userId: String,
    @field:NotNull val categoryId: UUID?,
    @field:NotBlank val name: String,
    val brand: String? = null,
    @field:PositiveOrZero val quantity: Int = 0,
    @field:PositiveOrZero val minStock: Int = 0,
    @field:PastOrPresent(message = "acquisitionDate no puede estar en el futuro")
    val acquisitionDate: LocalDate? = null,
    val expiryDate: LocalDate? = null,
    @field:DecimalMin(value = "0.0", inclusive = false, message = "price debe ser > 0")
    val price: BigDecimal? = null,
    val purchaseLocationId: UUID? = null,
    @field:Pattern(
        regexp = "^(https?://.*|)$",
        message = "imageUrl debe iniciar con http(s) o estar vacío"
    )
    val imageUrl: String? = null
)

// Actualización parcial: todo opcional
data class UpdateProductDto(
    val categoryId: UUID? = null,
    val name: String? = null,
    val brand: String? = null,
    @field:PositiveOrZero(message = "quantity debe ser >= 0")
    val quantity: Int? = null,
    @field:PositiveOrZero(message = "minStock debe ser >= 0")
    val minStock: Int? = null,
    @field:PastOrPresent(message = "acquisitionDate no puede estar en el futuro")
    val acquisitionDate: LocalDate? = null,
    val expiryDate: LocalDate? = null,
    @field:DecimalMin(value = "0.0", inclusive = false, message = "price debe ser > 0")
    val price: BigDecimal? = null,
    val purchaseLocationId: UUID? = null,
    @field:Pattern(
        regexp = "^(https?://.*|)$",
        message = "imageUrl debe iniciar con http(s) o estar vacío"
    )
    val imageUrl: String? = null
)

// ============================================================================
// MOVEMENT (Historial de movimientos: compras/consumos/ajustes)
// ============================================================================
data class MovementDto(
    val id: UUID,
    val productId: UUID,
    val userId: String,
    val storeId: UUID?,
    val type: MovementType,
    val quantity: Int,                 // >0 compra, <0 consumo, ajuste libre
    val unitPrice: BigDecimal?,
    val note: String?,
    val occurredAt: Instant
)

data class CreateMovementDto(
    @field:NotNull val productId: UUID?,
    @field:NotBlank val userId: String,
    val storeId: UUID? = null,
    @field:NotNull val type: MovementType?,
    // Validaciones de negocio se aplican en Service según type (compra/consumo/ajuste)
    val quantity: Int,
    @field:DecimalMin(value = "0.0", inclusive = false, message = "unitPrice debe ser > 0 para PURCHASE")
    val unitPrice: BigDecimal? = null,
    val note: String? = null,
    @field:NotNull val occurredAt: Instant?
)

// ============================================================================
// SHOPPING ITEM (Lista de compras)
// ============================================================================
data class ShoppingItemDto(
    val id: UUID,
    val userId: String,
    val productId: UUID,
    val desiredQuantity: Int,
    val isPurchased: Boolean,
    val source: ShoppingSource,
    val targetStoreId: UUID?,
    val createdAt: Instant,
    val purchasedAt: Instant?
)

data class CreateShoppingItemDto(
    @field:NotBlank val userId: String,
    @field:NotNull val productId: UUID?,
    @field:Positive val desiredQuantity: Int = 1,
    val source: ShoppingSource = ShoppingSource.MANUAL,
    val targetStoreId: UUID? = null
)

data class UpdateShoppingItemDto(
    @field:Positive(message = "desiredQuantity debe ser > 0")
    val desiredQuantity: Int? = null,
    val isPurchased: Boolean? = null,
    val source: ShoppingSource? = null,
    val targetStoreId: UUID? = null
)

// ============================================================================
// ALERT (Alertas por caducidad/stock bajo)
// ============================================================================
data class AlertDto(
    val id: UUID,
    val userId: String,
    val productId: UUID,
    val type: AlertType,
    val triggerAt: Instant,
    val isActive: Boolean,
    val resolvedAt: Instant?
)

data class CreateAlertDto(
    @field:NotBlank val userId: String,
    @field:NotNull val productId: UUID?,
    @field:NotNull val type: AlertType?,
    @field:NotNull val triggerAt: Instant?
)

data class UpdateAlertDto(
    val isActive: Boolean? = null,
    val resolvedAt: Instant? = null
)

// ============================================================================
// PRICE HISTORY (Evolución de precio por tienda)
// ============================================================================
data class PriceHistoryDto(
    val id: UUID,
    val productId: UUID,
    val storeId: UUID,
    val unitPrice: BigDecimal,
    val recordedAt: Instant
)

data class CreatePriceHistoryDto(
    @field:NotNull val productId: UUID?,
    @field:NotNull val storeId: UUID?,
    @field:DecimalMin(value = "0.0", inclusive = false) val unitPrice: BigDecimal?,
    @field:NotNull val recordedAt: Instant?
)

// ============================================================================
// PRODUCT RATING (Calificación del producto por usuario)
// ============================================================================
data class ProductRatingDto(
    val id: UUID,
    val userId: String,
    val productId: UUID,
    val qualityScore: Int,     // 1..5
    val notes: String?,
    val createdAt: Instant
)

data class CreateProductRatingDto(
    @field:NotBlank val userId: String,
    @field:NotNull val productId: UUID?,
    @field:Min(1) @field:Max(5) val qualityScore: Int,
    val notes: String? = null
)

data class UpdateProductRatingDto(
    @field:Min(1) @field:Max(5) val qualityScore: Int? = null,
    val notes: String? = null
)
