package cr.ac.una.homestock.data

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.*

// -----------------------------------------------------------------------------
// Enums (persistidos como STRING)
// -----------------------------------------------------------------------------
enum class MovementType { PURCHASE, CONSUMPTION, ADJUSTMENT }
enum class ShoppingSource { AUTO_RULE, MANUAL }
enum class AlertType { EXPIRY, LOW_STOCK }

// -----------------------------------------------------------------------------
// Núcleo
// -----------------------------------------------------------------------------
@Entity
@Table(
    name = "users",
    indexes = [Index(columnList = "email", unique = true)]
)
class UserEntity(
    @Id
    @Column(nullable = false, updatable = false)
    var id: String,                      // UID externo (Firebase, etc.)

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now()
)

@Entity
@Table(
    name = "categories",
    uniqueConstraints = [UniqueConstraint(columnNames = ["name"])]
)
class CategoryEntity(
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String
)

@Entity
@Table(
    name = "stores",
    indexes = [Index(columnList = "name")]
)
class StoreEntity(
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String,

    @Column
    var address: String? = null,

    @Column
    var district: String? = null,

    @Column
    var city: String? = null,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now()
)

@Entity
@Table(
    name = "products",
    indexes = [
        Index(columnList = "user_id,category_id"),
        Index(columnList = "name"),
        Index(columnList = "brand")
    ]
)
class ProductEntity(
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID = UUID.randomUUID(),

    // Dueño (multiusuario)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    // Categoría normalizada
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: CategoryEntity,

    @Column(nullable = false)
    var name: String,

    @Column
    var brand: String? = null,

    @Column(nullable = false)
    var quantity: Int = 0,

    @Column(nullable = false)
    var minStock: Int = 0,

    @Column
    var acquisitionDate: LocalDate? = null,

    @Column
    var expiryDate: LocalDate? = null,

    @Column(precision = 12, scale = 2)
    var price: BigDecimal? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_location_id")
    var purchaseLocation: StoreEntity? = null,

    @Column
    var imageUrl: String? = null,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    @PrePersist fun onCreate() {
        val now = Instant.now()
        createdAt = now; updatedAt = now
    }
    @PreUpdate fun onUpdate() { updatedAt = Instant.now() }
}

// -----------------------------------------------------------------------------
// Historial / Lista de compras / Alertas / Precios / Ratings
// -----------------------------------------------------------------------------
@Entity
@Table(
    name = "movements",
    indexes = [
        Index(columnList = "product_id,occurred_at"),
        Index(columnList = "user_id,occurred_at"),
        Index(columnList = "store_id,occurred_at")
    ]
)
class MovementEntity(
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    var store: StoreEntity? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: MovementType,

    @Column(nullable = false)
    var quantity: Int,

    @Column(precision = 12, scale = 2)
    var unitPrice: BigDecimal? = null,

    @Column
    var note: String? = null,

    @Column(nullable = false)
    var occurredAt: Instant
)

@Entity
@Table(
    name = "shopping_items",
    indexes = [
        Index(columnList = "user_id,is_purchased"),
        Index(columnList = "product_id")
    ]
)
class ShoppingItemEntity(
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity,

    @Column(nullable = false)
    var desiredQuantity: Int = 1,

    @Column(nullable = false)
    var isPurchased: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var source: ShoppingSource = ShoppingSource.MANUAL,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_store_id")
    var targetStore: StoreEntity? = null,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column
    var purchasedAt: Instant? = null
)

@Entity
@Table(
    name = "alerts",
    indexes = [Index(columnList = "user_id,is_active,trigger_at")]
)
class AlertEntity(
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: AlertType,

    @Column(nullable = false)
    var triggerAt: Instant,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column
    var resolvedAt: Instant? = null
)

@Entity
@Table(
    name = "price_history",
    indexes = [Index(columnList = "product_id,store_id,recorded_at")]
)
class PriceHistoryEntity(
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    var store: StoreEntity,

    @Column(nullable = false, precision = 12, scale = 2)
    var unitPrice: BigDecimal,

    @Column(nullable = false)
    var recordedAt: Instant
)

@Entity
@Table(
    name = "product_ratings",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "product_id"])],
    indexes = [Index(columnList = "user_id,product_id")]
)
class ProductRatingEntity(
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity,

    @Column(nullable = false)
    var qualityScore: Int,   // rango recomendado 1..5

    @Column
    var notes: String? = null,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now()
)
