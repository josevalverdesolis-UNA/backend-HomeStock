package cr.una.homestock.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

/* ======= ENUMS ======= */
enum class MovementType { PURCHASE, CONSUMPTION, ADJUSTMENT }
enum class ShoppingSource { AUTO_RULE, MANUAL }
enum class AlertType { EXPIRY, LOW_STOCK }

/* ======= USER ======= */
@Entity
@Table(
    name = "users",
    indexes = [Index(name = "idx_user_email", columnList = "email", unique = true)]
)
class User(
    @Id @UuidGenerator
    var id: String? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)

/* ======= CATEGORY ======= */
@Entity
@Table(
    name = "categories",
    indexes = [Index(name = "uq_category_name", columnList = "name", unique = true)]
)
class Category(
    @Id @UuidGenerator
    var id: String? = null,

    @Column(nullable = false, unique = true)
    var name: String
)

/* ======= STORE ======= */
@Entity
@Table(name = "stores")
class Store(
    @Id @UuidGenerator
    var id: String? = null,

    @Column(nullable = false)
    var name: String,

    var address: String? = null,
    var district: String? = null,
    var city: String? = null,

    @Column(nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)

/* ======= PRODUCT ======= */
@Entity
@Table(
    name = "products",
    indexes = [
        Index(name = "idx_product_user", columnList = "user_id"),
        Index(name = "idx_product_category", columnList = "category_id"),
        Index(name = "idx_product_name", columnList = "name"),
        Index(name = "idx_product_brand", columnList = "brand")
    ]
)
class Product(
    @Id @UuidGenerator
    var id: String? = null,

    @Column(name = "user_id", nullable = false)
    var userId: String,

    @Column(name = "category_id", nullable = false)
    var categoryId: String,

    @Column(nullable = false)
    var name: String,

    var brand: String? = null,

    @Column(nullable = false)
    var quantity: Int = 0,

    @Column(nullable = false)
    var minStock: Int = 0,

    var acquisitionDate: LocalDate? = null,
    var expiryDate: LocalDate? = null,
    var price: BigDecimal? = null,

    @Column(name = "purchase_location_id")
    var purchaseLocationId: String? = null,

    var imageUrl: String? = null,

    @Column(nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)

/* ======= MOVEMENT ======= */
@Entity
@Table(
    name = "movements",
    indexes = [
        Index(name = "idx_movement_product_time", columnList = "product_id, occurredAt"),
        Index(name = "idx_movement_user_time", columnList = "user_id, occurredAt")
    ]
)
class Movement(
    @Id @UuidGenerator
    var id: String? = null,

    @Column(name = "product_id", nullable = false)
    var productId: String,

    @Column(name = "user_id", nullable = false)
    var userId: String,

    @Column(name = "store_id")
    var storeId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: MovementType,

    @Column(nullable = false)
    var quantity: Int,

    var unitPrice: BigDecimal? = null,
    var note: String? = null,

    @Column(nullable = false)
    var occurredAt: OffsetDateTime = OffsetDateTime.now()
)

/* ======= SHOPPING ITEM ======= */
@Entity
@Table(
    name = "shopping_items",
    indexes = [
        Index(name = "idx_shopitem_user_purchased", columnList = "user_id, isPurchased")
    ]
)
class ShoppingItem(
    @Id @UuidGenerator
    var id: String? = null,

    @Column(name = "user_id", nullable = false)
    var userId: String,

    @Column(name = "product_id", nullable = false)
    var productId: String,

    @Column(nullable = false)
    var desiredQuantity: Int = 1,

    @Column(nullable = false)
    var isPurchased: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var source: ShoppingSource = ShoppingSource.MANUAL,

    @Column(name = "target_store_id")
    var targetStoreId: String? = null,

    @Column(nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    var purchasedAt: OffsetDateTime? = null
)

/* ======= ALERT ======= */
@Entity
@Table(
    name = "alerts",
    indexes = [
        Index(name = "idx_alert_user_active_time", columnList = "user_id, isActive, triggerAt")
    ]
)
class Alert(
    @Id @UuidGenerator
    var id: String? = null,

    @Column(name = "user_id", nullable = false)
    var userId: String,

    @Column(name = "product_id", nullable = false)
    var productId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: AlertType,

    @Column(nullable = false)
    var triggerAt: OffsetDateTime,

    @Column(nullable = false)
    var isActive: Boolean = true,

    var resolvedAt: OffsetDateTime? = null
)

/* ======= PRICE HISTORY ======= */
@Entity
@Table(
    name = "price_history",
    indexes = [
        Index(name = "idx_pricehist_product_store_time", columnList = "product_id, store_id, recordedAt")
    ]
)
class PriceHistory(
    @Id @UuidGenerator
    var id: String? = null,

    @Column(name = "product_id", nullable = false)
    var productId: String,

    @Column(name = "store_id", nullable = false)
    var storeId: String,

    @Column(nullable = false)
    var unitPrice: BigDecimal,

    @Column(nullable = false)
    var recordedAt: OffsetDateTime = OffsetDateTime.now()
)

/* ======= PRODUCT RATING ======= */
@Entity
@Table(
    name = "product_ratings",
    uniqueConstraints = [UniqueConstraint(name = "uq_rating_user_product", columnNames = ["user_id", "product_id"])]
)
class ProductRating(
    @Id @UuidGenerator
    var id: String? = null,

    @Column(name = "user_id", nullable = false)
    var userId: String,

    @Column(name = "product_id", nullable = false)
    var productId: String,

    @Column(nullable = false)
    var qualityScore: Int,

    var notes: String? = null,

    @Column(nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)
