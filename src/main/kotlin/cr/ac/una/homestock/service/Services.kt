package cr.ac.una.homestock.service

import cr.ac.una.homestock.domain.model.*
import cr.ac.una.homestock.dto.*
import cr.ac.una.homestock.data.*
import cr.ac.una.homestock.mapper.*
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

/* =========================
 * Services (interfaces)
 * ========================= */
interface CategoryService {
    fun create(input: CategoryInput): CategoryResult
}

interface StoreService {
    fun create(input: StoreInput): StoreResult
}

interface ProductService {
    fun create(input: ProductInput): ProductResult
    fun update(id: UUID, input: ProductUpdateInput): ProductResult
    fun findById(id: UUID): ProductResult
    fun listByUser(userId: UUID, pageable: Pageable): Page<ProductResult>
    fun listByUserAndCategory(userId: UUID, categoryId: UUID, pageable: Pageable): Page<ProductResult>
}

interface MovementService {
    fun create(input: MovementInput): MovementResult
}

interface ShoppingItemService {
    fun create(input: ShoppingItemInput): ShoppingItemResult
    fun markPurchased(id: UUID, input: ShoppingItemPurchaseInput): ShoppingItemResult
}

interface AlertService {
    fun create(input: AlertInput): AlertResult
    fun resolve(id: UUID, resolvedAt: OffsetDateTime = OffsetDateTime.now()): AlertResult
}

interface PriceHistoryService {
    fun register(input: PriceHistoryInput): PriceHistoryResult
}

interface ProductRatingService {
    fun rate(input: ProductRatingInput): ProductRatingResult
}

/* =========================
 * Services (impl)
 * ========================= */

@Service
class CategoryServiceImpl(
    private val users: UserRepository,
    private val categories: CategoryRepository,
    private val mapper: CategoryMapper
) : CategoryService {
    @Transactional
    override fun create(input: CategoryInput): CategoryResult {
        val user = users.findById(input.userId!!).orElseThrow { notFound("User", input.userId) }
        val entity = categories.save(Category(user = user, name = input.name.trim(), description = input.description))
        return mapper.toResult(entity)
    }
}

@Service
class StoreServiceImpl(
    private val users: UserRepository,
    private val stores: StoreRepository,
    private val mapper: StoreMapper
) : StoreService {
    @Transactional
    override fun create(input: StoreInput): StoreResult {
        val user = users.findById(input.userId!!).orElseThrow { notFound("User", input.userId) }
        val entity = stores.save(Store(user = user, name = input.name.trim(), address = input.address))
        return mapper.toResult(entity)
    }
}

@Service
class ProductServiceImpl(
    private val users: UserRepository,
    private val categories: CategoryRepository,
    private val products: ProductRepository,
    private val mapper: ProductMapper
) : ProductService {
    @Transactional
    override fun create(input: ProductInput): ProductResult {
        val user = users.findById(input.userId!!).orElseThrow { notFound("User", input.userId) }
        val category = categories.findById(input.categoryId!!).orElseThrow { notFound("Category", input.categoryId) }
        val entity = Product(
            user = user,
            category = category,
            name = input.name.trim(),
            brand = input.brand?.trim(),
            imageUrl = input.imageUrl?.trim(),
            quantity = 0,
            minStock = input.minStock
        )
        val saved = products.save(entity)
        return mapper.toResult(saved)
    }

    @Transactional
    override fun update(id: UUID, input: ProductUpdateInput): ProductResult {
        val e = products.findById(id).orElseThrow { notFound("Product", id) }
        input.categoryId?.let {
            val cat = categories.findById(it).orElseThrow { notFound("Category", it) }
            e.category = cat
        }
        input.name?.let { e.name = it.trim() }
        input.brand?.let { e.brand = it.trim() }
        input.imageUrl?.let { e.imageUrl = it.trim() }
        input.minStock?.let { require(it >= 0) { "minStock debe ser >= 0" }; e.minStock = it }
        return mapper.toResult(products.save(e))
    }

    override fun findById(id: UUID): ProductResult =
        mapper.toResult(products.findById(id).orElseThrow { notFound("Product", id) })

    override fun listByUser(userId: UUID, pageable: Pageable): Page<ProductResult> =
        products.findAllByUser_Id(userId, pageable).map { mapper.toResult(it) }

    override fun listByUserAndCategory(userId: UUID, categoryId: UUID, pageable: Pageable): Page<ProductResult> =
        products.findByUser_IdAndCategory_Id(userId, categoryId, pageable).map { mapper.toResult(it) }
}

@Service
class MovementServiceImpl(
    private val products: ProductRepository,
    private val stores: StoreRepository,
    private val movements: MovementRepository,
    private val shoppingItems: ShoppingItemRepository,
    private val mapper: MovementMapper
) : MovementService {

    @Transactional
    override fun create(input: MovementInput): MovementResult {
        val product = products.findById(input.productId!!).orElseThrow { notFound("Product", input.productId) }
        val store = input.storeId?.let { stores.findById(it).orElseThrow { notFound("Store", it) } }

        if (input.type == MovementType.PURCHASE) {
            require(input.unitPrice != null && input.unitPrice > BigDecimal.ZERO) {
                "unitPrice requerido y > 0 para movimientos de tipo PURCHASE"
            }
        }

        val movement = Movement(
            product = product,
            type = input.type!!,
            quantity = input.quantity,
            unitPrice = input.unitPrice,
            store = store,
            occurredAt = input.occurredAt ?: OffsetDateTime.now()
        )

        when (movement.type) {
            MovementType.PURCHASE -> product.quantity = product.quantity + movement.quantity
            MovementType.CONSUMPTION -> {
                val newQty = product.quantity - movement.quantity
                require(newQty >= 0) { "Stock insuficiente: intentas consumir ${movement.quantity} y hay ${product.quantity}" }
                product.quantity = newQty
            }
        }

        movements.save(movement)
        products.save(product)
        ensureAutoShoppingItem(product, shoppingItems)
        return mapper.toResult(movement)
    }

    private fun ensureAutoShoppingItem(product: Product, shoppingItems: ShoppingItemRepository) {
        if (product.quantity < product.minStock) {
            val suggestedQty = (product.minStock - product.quantity).coerceAtLeast(1)
            val item = ShoppingItem(
                user = product.user,
                product = product,
                quantity = suggestedQty,
                targetStore = null,
                purchasedAt = null,
                createdAt = OffsetDateTime.now(),
                source = "AUTO_LOW_STOCK"
            )
            shoppingItems.save(item)
        }
    }
}

@Service
class ShoppingItemServiceImpl(
    private val shoppingItems: ShoppingItemRepository,
    private val users: UserRepository,
    private val products: ProductRepository,
    private val stores: StoreRepository,
    private val mapper: ShoppingItemMapper
) : ShoppingItemService {

    @Transactional
    override fun create(input: ShoppingItemInput): ShoppingItemResult {
        val userRef = users.getReferenceById(input.userId!!)
        val productRef = products.getReferenceById(input.productId!!)
        val targetStoreRef = input.targetStoreId?.let { stores.getReferenceById(it) }
        val item = ShoppingItem(
            user = userRef,
            product = productRef,
            quantity = input.quantity,
            targetStore = targetStoreRef,
            purchasedAt = null,
            createdAt = OffsetDateTime.now(),
            source = "MANUAL"
        )
        val saved = shoppingItems.save(item)
        return mapper.toResult(saved)
    }

    @Transactional
    override fun markPurchased(id: UUID, input: ShoppingItemPurchaseInput): ShoppingItemResult {
        val e = shoppingItems.findById(id).orElseThrow { notFound("ShoppingItem", id) }
        e.purchasedAt = input.purchasedAt
        return mapper.toResult(shoppingItems.save(e))
    }
}

@Service
class AlertServiceImpl(
    private val alerts: AlertRepository,
    private val users: UserRepository,
    private val mapper: AlertMapper
) : AlertService {
    @Transactional
    override fun create(input: AlertInput): AlertResult {
        val userRef = users.getReferenceById(input.userId!!)
        val e = Alert(
            user = userRef,
            type = input.type!!,
            message = input.message,
            triggerAt = input.triggerAt ?: OffsetDateTime.now(),
            isActive = true
        )
        return mapper.toResult(alerts.save(e))
    }

    @Transactional
    override fun resolve(id: UUID, resolvedAt: OffsetDateTime): AlertResult {
        val e = alerts.findById(id).orElseThrow { notFound("Alert", id) }
        e.isActive = false
        e.resolvedAt = resolvedAt
        return mapper.toResult(alerts.save(e))
    }
}

@Service
class PriceHistoryServiceImpl(
    private val products: ProductRepository,
    private val stores: StoreRepository,
    private val histories: PriceHistoryRepository,
    private val mapper: PriceHistoryMapper
) : PriceHistoryService {
    @Transactional
    override fun register(input: PriceHistoryInput): PriceHistoryResult {
        val productRef = products.getReferenceById(input.productId!!)
        val storeRef = input.storeId?.let { stores.getReferenceById(it) }
        val e = PriceHistory(product = productRef, store = storeRef, price = input.price, registeredAt = OffsetDateTime.now())
        return mapper.toResult(histories.save(e))
    }
}

@Service
class ProductRatingServiceImpl(
    private val users: UserRepository,
    private val products: ProductRepository,
    private val ratings: ProductRatingRepository,
    private val mapper: ProductRatingMapper
) : ProductRatingService {
    @Transactional
    override fun rate(input: ProductRatingInput): ProductRatingResult {
        val userRef = users.getReferenceById(input.userId!!)
        val productRef = products.getReferenceById(input.productId!!)
        val e = ProductRating(user = userRef, product = productRef, score = input.score, comment = input.comment)
        return mapper.toResult(ratings.save(e))
    }
}

/* =========================
 * Helpers
 * ========================= */
private fun notFound(entity: String, id: Any?): EntityNotFoundException =
    EntityNotFoundException("$entity not found: $id")
