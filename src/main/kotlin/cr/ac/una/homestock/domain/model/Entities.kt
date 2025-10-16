package cr.ac.una.homestock.domain.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

/* =========================
 * Enums
 * ========================= */
enum class MovementType { PURCHASE, CONSUMPTION }
enum class AlertType { GENERIC, LOW_STOCK, EXPIRY, PRICE_DROP }

/* =========================
 * Base
 * ========================= */
@MappedSuperclass
abstract class BaseEntity {
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID = UUID.randomUUID()
}

/* =========================
 * User
 * ========================= */
@Entity
@Table(name = "users", indexes = [Index(name = "ix_users_email", columnList = "email", unique = true)])
class User(
    @Column(nullable = false) var name: String,
    @Column(nullable = false, unique = true) var email: String
) : BaseEntity()

/* =========================
 * Category
 * ========================= */
@Entity
@Table(
    name = "categories",
    uniqueConstraints = [UniqueConstraint(name = "uq_category_user_name", columnNames = ["user_id", "name"])],
    indexes = [Index(name = "ix_categories_user", columnList = "user_id")]
)
class Category(
    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false)
    var user: User,
    @Column(nullable = false) var name: String,
    var description: String? = null
) : BaseEntity()

/* =========================
 * Store
 * ========================= */
@Entity
@Table(
    name = "stores",
    uniqueConstraints = [UniqueConstraint(name = "uq_store_user_name", columnNames = ["user_id", "name"])],
    indexes = [Index(name = "ix_stores_user", columnList = "user_id")]
)
class Store(
    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false)
    var user: User,
    @Column(nullable = false) var name: String,
    var address: String? = null
) : BaseEntity()

/* =========================
 * Product
 * ========================= */
@Entity
@Table(
    name = "products",
    uniqueConstraints = [UniqueConstraint(name = "uq_product_user_name", columnNames = ["user_id", "name"])],
    indexes = [
        Index(name = "ix_products_user", columnList = "user_id"),
        Index(name = "ix_products_category", columnList = "category_id")
    ]
)
class Product(
    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(optional = false) @JoinColumn(name = "category_id", nullable = false)
    var category: Category,

    @Column(nullable = false) var name: String,
    var brand: String? = null,
    var imageUrl: String? = null,

    /** Stock actual mantenido por movimientos (no modificar directamente en flujo normal) */
    @Column(nullable = false) var quantity: Int = 0,

    /** Stock mínimo deseado para auto-generar ShoppingItem */
    @Column(nullable = false) var minStock: Int = 1
) : BaseEntity() {
    /** Locking optimista para evitar carreras al actualizar stock */
    @Version
    @Column(nullable = false)
    var version: Long = 0
}

/* =========================
 * Movement
 * ========================= */
@Entity
@Table(
    name = "movements",
    indexes = [
        Index(name = "ix_movements_product", columnList = "product_id"),
        Index(name = "ix_movements_store", columnList = "store_id"),
        Index(name = "ix_movements_time", columnList = "occurred_at")
    ]
)
class Movement(
    @ManyToOne(optional = false) @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    var type: MovementType,

    /** Cantidad positiva (se interpreta según type) */
    @Column(nullable = false)
    var quantity: Int,

    /** Precio unitario solo aplica para PURCHASE */
    var unitPrice: BigDecimal? = null,

    /** Tienda donde ocurrió la compra (opcional para CONSUMPTION) */
    @ManyToOne @JoinColumn(name = "store_id")
    var store: Store? = null,

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: OffsetDateTime = OffsetDateTime.now()
) : BaseEntity()

/* =========================
 * ShoppingItem
 * ========================= */
@Entity
@Table(
    name = "shopping_items",
    indexes = [
        Index(name = "ix_shopping_items_user", columnList = "user_id"),
        Index(name = "ix_shopping_items_product", columnList = "product_id"),
        Index(name = "ix_shopping_items_target_store", columnList = "target_store_id")
    ]
)
class ShoppingItem(
    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(optional = false) @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Column(nullable = false) var quantity: Int = 1,

    /** Sugerencia de tienda a comprar */
    @ManyToOne @JoinColumn(name = "target_store_id")
    var targetStore: Store? = null,

    /** Marcado cuando se compra efectivamente */
    var purchasedAt: OffsetDateTime? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    /** Fuente del ítem (manual, auto por low stock, etc.) */
    var source: String? = "AUTO_LOW_STOCK"
) : BaseEntity()

/* =========================
 * Alert
 * ========================= */
@Entity
@Table(
    name = "alerts",
    indexes = [
        Index(name = "ix_alerts_user", columnList = "user_id"),
        Index(name = "ix_alerts_active", columnList = "is_active"),
        Index(name = "ix_alerts_trigger_at", columnList = "trigger_at")
    ]
)
class Alert(
    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    var type: AlertType = AlertType.GENERIC,

    var message: String? = null,

    @Column(name = "trigger_at") var triggerAt: OffsetDateTime? = null,
    @Column(name = "is_active", nullable = false) var isActive: Boolean = true,
    @Column(name = "resolved_at") var resolvedAt: OffsetDateTime? = null
) : BaseEntity()

/* =========================
 * PriceHistory
 * ========================= */
@Entity
@Table(
    name = "price_history",
    indexes = [
        Index(name = "ix_price_history_product_store_time", columnList = "product_id,store_id,registered_at")
    ]
)
class PriceHistory(
    @ManyToOne(optional = false) @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @ManyToOne @JoinColumn(name = "store_id")
    var store: Store? = null,

    @Column(nullable = false) var price: BigDecimal,

    @Column(name = "registered_at", nullable = false)
    var registeredAt: OffsetDateTime = OffsetDateTime.now()
) : BaseEntity()

/* =========================
 * ProductRating
 * ========================= */
@Entity
@Table(
    name = "product_ratings",
    uniqueConstraints = [UniqueConstraint(name = "uq_rating_user_product", columnNames = ["user_id", "product_id"])],
    indexes = [
        Index(name = "ix_ratings_product", columnList = "product_id"),
        Index(name = "ix_ratings_user", columnList = "user_id")
    ]
)
class ProductRating(
    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(optional = false) @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Column(nullable = false) var score: Int, // 1..5
    var comment: String? = null
) : BaseEntity()
