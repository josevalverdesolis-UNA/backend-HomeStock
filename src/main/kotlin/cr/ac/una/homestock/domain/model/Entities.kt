package cr.una.homestock.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

/* =========================
   ENUMS
   ========================= */
enum class MovementType { PURCHASE, CONSUMPTION }
enum class ShoppingSource { MANUAL, AUTO_RULE }
enum class AlertType { LOW_STOCK, EXPIRY }

/* =========================
   USER
   ========================= */
@Entity @Table(name = "users")
class User(
    @Id @GeneratedValue @UuidGenerator
    var id: String? = null,

    @Column(nullable = false, length = 120)
    var name: String,

    @Column(nullable = false, length = 160, unique = true)
    var email: String
)

/* =========================
   CATEGORY
   ========================= */
@Entity
@Table(
    name = "categories",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "name"])],
    indexes = [Index(name = "idx_category_user", columnList = "user_id")]
)
class Category(
    @Id @GeneratedValue @UuidGenerator
    var id: String? = null,

    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false, length = 100)
    var name: String
)

/* =========================
   STORE
   ========================= */
@Entity
@Table(
    name = "stores",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "name"])],
    indexes = [Index(name = "idx_store_user", columnList = "user_id")]
)
class Store(
    @Id @GeneratedValue @UuidGenerator
    var id: String? = null,

    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false, length = 140)
    var name: String,

    @Column(length = 180)
    var location: String? = null
)

/* =========================
   PRODUCT
   ========================= */
@Entity
@Table(
    name = "products",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "name", "brand"])],
    indexes = [
        Index(name = "idx_products_user_cat", columnList = "user_id, category_id"),
        Index(name = "idx_products_name", columnList = "name")
    ]
)
class Product(
    @Id @GeneratedValue @UuidGenerator
    var id: String? = null,

    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne @JoinColumn(name = "category_id")
    var category: Category? = null,

    @ManyToOne @JoinColumn(name = "store_id")
    var store: Store? = null,

    @Column(nullable = false, length = 160)
    var name: String,

    @Column(length = 120)
    var brand: String? = null,

    @Column(nullable = false)
    var quantity: Int = 0,

    @Column(nullable = false)
    var minStock: Int = 0,

    var acquisitionDate: LocalDate? = null,

    @Column(precision = 12, scale = 2)
    var price: BigDecimal? = null,

    @Column(length = 300)
    var imageUrl: String? = null
)

/* =========================
   MOVEMENT
   ========================= */
@Entity
@Table(
    name = "movements",
    indexes = [Index(name = "idx_movements_product_time", columnList = "product_id, occurred_at")]
)
class Movement(
    @Id @GeneratedValue @UuidGenerator
    var id: String? = null,

    @ManyToOne(optional = false) @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var type: MovementType,

    @Column(nullable = false)
    var quantity: Int,

    @Column(precision = 12, scale = 2)
    var unitPrice: BigDecimal? = null,

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(length = 280)
    var note: String? = null
)

/* =========================
   SHOPPING ITEM
   ========================= */
@Entity
@Table(
    name = "shopping_items",
    indexes = [
        Index(name = "idx_shopping_active", columnList = "product_id")
    ]
)
class ShoppingItem(
    @Id @GeneratedValue @UuidGenerator
    var id: String? = null,

    @ManyToOne(optional = false) @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Column(nullable = false)
    var quantity: Int,

    @Column(nullable = false)
    var isPurchased: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var source: ShoppingSource = ShoppingSource.MANUAL,

    @Column(nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)

/* =========================
   ALERT
   ========================= */
@Entity
@Table(
    name = "alerts",
    indexes = [Index(name = "idx_alerts_user_created", columnList = "user_id, created_at")]
)
class Alert(
    @Id @GeneratedValue @UuidGenerator
    var id: String? = null,

    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne @JoinColumn(name = "product_id")
    var product: Product? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var type: AlertType,

    @Column(nullable = false, length = 240)
    var message: String,

    @Column(nullable = false, name = "created_at")
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(nullable = false)
    var resolved: Boolean = false
)

/* =========================
   PRICE HISTORY
   ========================= */
@Entity
@Table(
    name = "price_history",
    indexes = [Index(name = "idx_price_product_time", columnList = "product_id, registered_at")]
)
class PriceHistory(
    @Id @GeneratedValue @UuidGenerator
    var id: String? = null,

    @ManyToOne(optional = false) @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Column(nullable = false, precision = 12, scale = 2)
    var price: BigDecimal,

    @Column(nullable = false, name = "registered_at")
    var registeredAt: OffsetDateTime = OffsetDateTime.now()
)

/* =========================
   PRODUCT RATING
   ========================= */
@Entity
@Table(
    name = "product_ratings",
    indexes = [Index(name = "idx_rating_product_time", columnList = "product_id, created_at")]
)
class ProductRating(
    @Id @GeneratedValue @UuidGenerator
    var id: String? = null,

    @ManyToOne(optional = false) @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Column(nullable = false)
    var score: Int, // 1..5

    @Column(length = 300)
    var comment: String? = null,

    @Column(nullable = false, name = "created_at")
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)
