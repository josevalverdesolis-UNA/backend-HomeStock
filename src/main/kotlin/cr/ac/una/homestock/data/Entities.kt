package cr.ac.una.homestock.data

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

/* Técnicas:
   - Identidad por String (compat REST/Room/Firebase).
   - equals/hashCode por id (y email en User).
   - toString() seguro (evita LAZY).
   - Fechas con java.util.Date + @Temporal. */

private fun sameId(a: Any?, b: Any?) = a != null && a == b

/* Enumeraciones */
enum class MovementType { PURCHASE, CONSUMPTION, ADJUSTMENT }
enum class ShoppingSource { AUTO_RULE, MANUAL }
enum class AlertType { EXPIRY, LOW_STOCK }

/* User */
@Entity @Table(
    name = "users",
    uniqueConstraints = [UniqueConstraint(name = "uk_users_email", columnNames = ["email"])]
)
class UserEntity(
    @Id @Column(length = 64) var id: String,
    @Column(nullable = false, length = 120) var name: String,
    @Column(nullable = false, length = 160) var email: String,
    @Temporal(TemporalType.TIMESTAMP) @Column(nullable = false) var createdAt: Date = Date(),
) {
    override fun toString() = "User(id=$id,email=$email)"
    override fun equals(other: Any?) = other is UserEntity && (sameId(id, other.id) || email.equals(other.email, true))
    override fun hashCode() = id.hashCode()
}

/* Category */
@Entity @Table(
    name = "categories",
    uniqueConstraints = [UniqueConstraint(name = "uk_category_name", columnNames = ["name"])],
    indexes = [Index(name = "ix_categories_name", columnList = "name")]
)
class CategoryEntity(
    @Id @Column(length = 64) var id: String,
    @Column(nullable = false, length = 80) var name: String,
) {
    override fun toString() = "Category(id=$id,name=$name)"
    override fun equals(other: Any?) = other is CategoryEntity && sameId(id, other.id)
    override fun hashCode() = id.hashCode()
}

/* Store */
@Entity @Table(
    name = "stores",
    indexes = [Index(name = "ix_stores_name", columnList = "name")]
)
class StoreEntity(
    @Id @Column(length = 64) var id: String,
    @Column(nullable = false, length = 120) var name: String,
    @Column(length = 200) var address: String? = null,
    @Column(length = 120) var district: String? = null,
    @Column(length = 120) var city: String? = null,
    @Temporal(TemporalType.TIMESTAMP) @Column(nullable = false) var createdAt: Date = Date(),
) {
    override fun toString() = "Store(id=$id,name=$name)"
    override fun equals(other: Any?) = other is StoreEntity && sameId(id, other.id)
    override fun hashCode() = id.hashCode()
}

/* Product */
@Entity @Table(
    name = "products",
    indexes = [
        Index(name = "ix_products_user", columnList = "user_id"),
        Index(name = "ix_products_category", columnList = "category_id"),
        Index(name = "ix_products_name", columnList = "name"),
        Index(name = "ix_products_brand", columnList = "brand")
    ]
)
class ProductEntity(
    @Id @Column(length = 64) var id: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "category_id", nullable = false)
    var category: CategoryEntity,
    @Column(nullable = false, length = 120) var name: String,
    @Column(length = 80) var brand: String? = null,
    @Column(nullable = false) var quantity: Int = 0,
    @Column(nullable = false) var minStock: Int = 0,
    @Temporal(TemporalType.TIMESTAMP) var acquisitionDate: Date? = null,
    @Temporal(TemporalType.TIMESTAMP) var expiryDate: Date? = null,
    @Column(precision = 12, scale = 2) var price: BigDecimal? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "purchase_location_id")
    var purchaseLocation: StoreEntity? = null,
    @Column(length = 512) var imageUrl: String? = null,
    @Temporal(TemporalType.TIMESTAMP) @Column(nullable = false) var createdAt: Date = Date(),
    @Temporal(TemporalType.TIMESTAMP) @Column(nullable = false) var updatedAt: Date = Date(),
) {
    @PreUpdate fun onUpdate() { updatedAt = Date() }
    override fun toString() = "Product(id=$id,name=$name,qty=$quantity)"
    override fun equals(other: Any?) = other is ProductEntity && sameId(id, other.id)
    override fun hashCode() = id.hashCode()
}

/* Movement */
@Entity @Table(
    name = "movements",
    indexes = [
        Index(name = "ix_movements_product", columnList = "product_id,occurred_at"),
        Index(name = "ix_movements_user", columnList = "user_id,occurred_at"),
        Index(name = "ix_movements_store", columnList = "store_id,occurred_at")
    ]
)
class MovementEntity(
    @Id @Column(length = 64) var id: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "store_id")
    var store: StoreEntity? = null,
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) var type: MovementType,
    @Column(nullable = false) var quantity: Int,
    @Column(precision = 12, scale = 2) var unitPrice: BigDecimal? = null,
    @Column(length = 200) var note: String? = null,
    @Temporal(TemporalType.TIMESTAMP) @Column(name = "occurred_at", nullable = false) var occurredAt: Date = Date(),
) {
    override fun toString() = "Movement(id=$id,type=$type,qty=$quantity)"
    override fun equals(other: Any?) = other is MovementEntity && sameId(id, other.id)
    override fun hashCode() = id.hashCode()
}

/* ShoppingItem */
@Entity @Table(
    name = "shopping_items",
    indexes = [
        Index(name = "ix_shopping_user", columnList = "user_id,is_purchased"),
        Index(name = "ix_shopping_product", columnList = "product_id")
    ]
)
class ShoppingItemEntity(
    @Id @Column(length = 64) var id: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity,
    @Column(nullable = false) var desiredQuantity: Int = 1,
    @Column(name = "is_purchased", nullable = false) var isPurchased: Boolean = false,
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) var source: ShoppingSource = ShoppingSource.MANUAL,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "target_store_id")
    var targetStore: StoreEntity? = null,
    @Temporal(TemporalType.TIMESTAMP) @Column(nullable = false) var createdAt: Date = Date(),
    @Temporal(TemporalType.TIMESTAMP) var purchasedAt: Date? = null,
) {
    override fun toString() = "ShoppingItem(id=$id,isPurchased=$isPurchased)"
    override fun equals(other: Any?) = other is ShoppingItemEntity && sameId(id, other.id)
    override fun hashCode() = id.hashCode()
}

/* Alert */
@Entity @Table(
    name = "alerts",
    indexes = [Index(name = "ix_alerts_user_active", columnList = "user_id,is_active,trigger_at")]
)
class AlertEntity(
    @Id @Column(length = 64) var id: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity,
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) var type: AlertType,
    @Temporal(TemporalType.TIMESTAMP) @Column(nullable = false) var triggerAt: Date,
    @Column(name = "is_active", nullable = false) var isActive: Boolean = true,
    @Temporal(TemporalType.TIMESTAMP) var resolvedAt: Date? = null,
) {
    override fun toString() = "Alert(id=$id,type=$type,active=$isActive)"
    override fun equals(other: Any?) = other is AlertEntity && sameId(id, other.id)
    override fun hashCode() = id.hashCode()
}

/* PriceHistory */
@Entity @Table(
    name = "price_history",
    indexes = [Index(name = "ix_price_hist", columnList = "product_id,store_id,recorded_at")]
)
class PriceHistoryEntity(
    @Id @Column(length = 64) var id: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "store_id", nullable = false)
    var store: StoreEntity,
    @Column(nullable = false, precision = 12, scale = 2) var unitPrice: BigDecimal,
    @Temporal(TemporalType.TIMESTAMP) @Column(name = "recorded_at", nullable = false) var recordedAt: Date = Date(),
) {
    override fun toString() = "PriceHistory(id=$id,price=$unitPrice)"
    override fun equals(other: Any?) = other is PriceHistoryEntity && sameId(id, other.id)
    override fun hashCode() = id.hashCode()
}

/* ProductRating */
@Entity @Table(
    name = "product_ratings",
    uniqueConstraints = [UniqueConstraint(name = "uk_rating_user_product", columnNames = ["user_id", "product_id"])]
)
class ProductRatingEntity(
    @Id @Column(length = 64) var id: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity,
    @Column(nullable = false) var qualityScore: Int,
    @Column(length = 300) var notes: String? = null,
    @Temporal(TemporalType.TIMESTAMP) @Column(nullable = false) var createdAt: Date = Date(),
) {
    override fun toString() = "ProductRating(id=$id,score=$qualityScore)"
    override fun equals(other: Any?) = other is ProductRatingEntity && sameId(id, other.id)
    override fun hashCode() = id.hashCode()
}
