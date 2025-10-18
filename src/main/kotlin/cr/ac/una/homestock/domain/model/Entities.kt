@file:Suppress("unused")

package cr.ac.una.homestock.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

/**
 * Entidades JPA alineadas con el ER de HomeStock.
 * - Nombres y tipos sincronizados con DTOs y especificación.
 * - Índices y restricciones únicas clave.
 * - Timestamps automáticos por @PrePersist/@PreUpdate.
 */

// ------------------------------
// Enums de dominio (idénticos al contrato API)
// ------------------------------

enum class MovementType { PURCHASE, CONSUMPTION, ADJUSTMENT }

enum class ShoppingSource { AUTO_RULE, MANUAL }

enum class AlertType { LOW_STOCK, EXPIRY }

// ------------------------------
// Auditoría simple
// ------------------------------

@MappedSuperclass
open class Auditable {
    @Column(name = "created_at", nullable = false)
    open var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    open var updatedAt: Instant = Instant.now()

    @PrePersist
    open fun onCreate() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    open fun onUpdate() {
        updatedAt = Instant.now()
    }
}

// ------------------------------
// User
// ------------------------------

@Entity
@Table(
    name = "users",
    indexes = [Index(name = "ix_users_email", columnList = "email", unique = true)]
)
open class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(nullable = false)
    open var name: String = "",

    @Column(nullable = false, unique = true)
    open var email: String = "",

    // Nuevos campos para autenticación
    @Column(name = "password_hash", nullable = false)
    open var passwordHash: String = "",

    @Column(nullable = false)
    open var role: String = "USER",
) : Auditable()

// ------------------------------
// Category (global, no por usuario)
// ------------------------------

@Entity
@Table(
    name = "categories",
    uniqueConstraints = [UniqueConstraint(name = "uk_category_name", columnNames = ["name"])],
    indexes = [Index(name = "ix_category_name", columnList = "name", unique = true)]
)
open class Category(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(nullable = false)
    open var name: String = "",

    @Column
    open var description: String? = null,
) : Auditable()

// ------------------------------
// Store (lugar de compra)
// ------------------------------

@Entity
@Table(
    name = "stores",
    uniqueConstraints = [UniqueConstraint(name = "uk_store_name_location", columnNames = ["name", "location"])],
    indexes = [Index(name = "ix_store_name_location", columnList = "name, location", unique = true)]
)
open class Store(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(nullable = false)
    open var name: String = "",

    @Column
    open var location: String? = null,

    @Column
    open var notes: String? = null,
) : Auditable()

// ------------------------------
// Product
// ------------------------------

@Entity
@Table(
    name = "products",
    indexes = [
        Index(name = "ix_product_user", columnList = "user_id"),
        Index(name = "ix_product_category", columnList = "category_id"),
        Index(name = "ix_product_store", columnList = "purchase_location_id"),
        Index(name = "ix_product_name", columnList = "name")
    ]
)
open class Product(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    open var user: User? = null,

    @Column(nullable = false)
    open var name: String = "",

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id", nullable = false)
    open var category: Category? = null,

    @Column(nullable = false)
    open var quantity: Int = 0,

    @Column(name = "min_stock", nullable = false)
    open var minStock: Int = 0,

    @Column(name = "acquisition_date")
    open var acquisitionDate: LocalDate? = null,

    @Column(name = "expiry_date")
    open var expiryDate: LocalDate? = null,

    @Column(precision = 19, scale = 4)
    open var price: BigDecimal? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "purchase_location_id")
    open var purchaseLocation: Store? = null,

    @Column
    open var brand: String? = null,

    @Column(name = "image_url")
    open var imageUrl: String? = null,
) : Auditable()

// ------------------------------
// Movement (impacta inventario)
// ------------------------------

@Entity
@Table(
    name = "movements",
    indexes = [
        Index(name = "ix_movement_user", columnList = "user_id"),
        Index(name = "ix_movement_product", columnList = "product_id"),
        Index(name = "ix_movement_store", columnList = "store_id"),
        Index(name = "ix_movement_created", columnList = "created_at"),
        Index(name = "ix_movement_occurred", columnList = "occurred_at")
    ]
)
open class Movement(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    open var user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false)
    open var product: Product? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    open var type: MovementType = MovementType.PURCHASE,

    /**
     * quantity se almacena siempre positiva; el signo efectivo se aplica según el tipo en servicio.
     */
    @Column(nullable = false)
    open var quantity: Int = 0,

    @Column(precision = 19, scale = 4)
    open var unitPrice: BigDecimal? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "store_id")
    open var store: Store? = null,

    @Column
    open var note: String? = null,

    @Column(name = "occurred_at", nullable = false)
    open var occurredAt: Instant = Instant.now(),
) : Auditable()

// ------------------------------
// ShoppingItem (lista de compras)
// ------------------------------

@Entity
@Table(
    name = "shopping_items",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_shopping_user_product_active",
            columnNames = ["user_id", "product_id", "is_purchased"]
        )
    ],
    indexes = [
        Index(name = "ix_shopping_user", columnList = "user_id"),
        Index(name = "ix_shopping_product", columnList = "product_id"),
        Index(name = "ix_shopping_is_purchased", columnList = "is_purchased"),
        Index(name = "ix_shopping_target_store", columnList = "target_store_id")
    ]
)
open class ShoppingItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    open var user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false)
    open var product: Product? = null,

    @Column(name = "desired_quantity", nullable = false)
    open var desiredQuantity: Int = 1,

    @Column(name = "is_purchased", nullable = false)
    open var purchased: Boolean = false,

    @Column
    open var purchasedAt: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    open var source: ShoppingSource = ShoppingSource.MANUAL,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "target_store_id")
    open var targetStore: Store? = null,
) : Auditable()

// ------------------------------
// Alertas
// ------------------------------

@Entity
@Table(
    name = "alerts",
    indexes = [
        Index(name = "ix_alert_user", columnList = "user_id"),
        Index(name = "ix_alert_product", columnList = "product_id"),
        Index(name = "ix_alert_trigger", columnList = "trigger_at"),
        Index(name = "ix_alert_active", columnList = "is_active")
    ]
)
open class Alert(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    open var user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id")
    open var product: Product? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    open var type: AlertType = AlertType.LOW_STOCK,

    @Column
    open var message: String? = null,

    @Column(name = "trigger_at", nullable = false)
    open var triggerAt: Instant = Instant.now(),

    @Column(name = "is_active", nullable = false)
    open var active: Boolean = true,

    @Column
    open var resolvedAt: Instant? = null,
) : Auditable()

// ------------------------------
// PriceHistory (histórico de precios)
// ------------------------------

@Entity
@Table(
    name = "price_history",
    indexes = [
        Index(name = "ix_ph_product", columnList = "product_id"),
        Index(name = "ix_ph_store", columnList = "store_id"),
        Index(name = "ix_ph_recorded", columnList = "recorded_at")
    ]
)
open class PriceHistory(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false)
    open var product: Product? = null,

    @Column(nullable = false, precision = 19, scale = 4)
    open var unitPrice: BigDecimal = BigDecimal.ZERO,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "store_id")
    open var store: Store? = null,

    @Column(name = "recorded_at", nullable = false)
    open var recordedAt: Instant = Instant.now(),
) : Auditable()

// ------------------------------
// ProductRating (valoración)
// ------------------------------

@Entity
@Table(
    name = "product_ratings",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_rating_user_product", columnNames = ["user_id", "product_id"])
    ],
    indexes = [
        Index(name = "ix_rating_user", columnList = "user_id"),
        Index(name = "ix_rating_product", columnList = "product_id")
    ]
)
open class ProductRating(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    open var user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false)
    open var product: Product? = null,

    @Column(nullable = false)
    open var qualityScore: Int = 1, // 1..5

    @Column
    open var notes: String? = null,
) : Auditable()
