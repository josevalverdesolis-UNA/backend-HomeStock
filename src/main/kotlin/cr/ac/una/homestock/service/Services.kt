package cr.ac.una.homestock.service

import cr.ac.una.homestock.data.*
import cr.ac.una.homestock.dto.*
import cr.ac.una.homestock.mapper.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/* Interfaces */
interface UserService {
    fun list(): List<UserResult>
    fun get(id: String): UserResult
    fun create(input: UserInput): UserResult
    fun update(id: String, input: UserInput): UserResult
    fun delete(id: String)
}

interface CategoryService {
    fun list(): List<CategoryResult>
    fun get(id: String): CategoryResult
    fun create(input: CategoryInput): CategoryResult
    fun update(id: String, input: CategoryInput): CategoryResult
    fun delete(id: String)
}

interface StoreService {
    fun list(): List(StoreResult)
    fun get(id: String): StoreResult
    fun create(input: StoreInput): StoreResult
    fun update(id: String, input: StoreInput): StoreResult
    fun delete(id: String)
}

interface ProductService {
    fun list(userId: String?): List<ProductResult>
    fun get(id: String): ProductResult
    fun create(input: ProductInput): ProductResult
    fun update(id: String, input: ProductInput): ProductResult
    fun delete(id: String)
}

interface MovementService {
    fun list(userId: String?): List<MovementResult>
    fun get(id: String): MovementResult
    fun create(input: MovementInput): MovementResult
    fun delete(id: String)
}

interface ShoppingItemService {
    fun list(userId: String?, purchased: Boolean?): List<ShoppingItemResult>
    fun get(id: String): ShoppingItemResult
    fun create(input: ShoppingItemInput): ShoppingItemResult
    fun update(id: String, input: ShoppingItemInput): ShoppingItemResult
    fun delete(id: String)
}

interface AlertService {
    fun list(userId: String?): List<AlertResult>
    fun get(id: String): AlertResult
    fun create(input: AlertInput): AlertResult
    fun update(id: String, input: AlertInput): AlertResult
    fun delete(id: String)
}

interface PriceHistoryService {
    fun list(productId: String?, storeId: String?): List<PriceHistoryResult>
    fun get(id: String): PriceHistoryResult
    fun create(input: PriceHistoryInput): PriceHistoryResult
}

interface ProductRatingService {
    fun list(userId: String?, productId: String?): List<ProductRatingResult>
    fun get(id: String): ProductRatingResult
    fun upsert(input: ProductRatingInput): ProductRatingResult
    fun delete(id: String)
}

/* Implementaciones */

@Service
class UserServiceImpl(
    private val repo: UserRepository,
    private val mapper: UserMapper
) : UserService {
    override fun list() = repo.findAll().map(mapper::toResult)
    override fun get(id: String) = mapper.toResult(repo.findById(id).orElseThrow { NoSuchElementException("User $id") })

    @Transactional
    override fun create(input: UserInput): UserResult {
        val id = input.id?.trim() ?: throw IllegalArgumentException("id is required")
        val name = input.name?.trim() ?: throw IllegalArgumentException("name is required")
        val email = input.email?.trim() ?: throw IllegalArgumentException("email is required")
        if (repo.existsByEmailIgnoreCase(email)) throw IllegalArgumentException("email already exists")
        return mapper.toResult(repo.save(UserEntity(id = id, name = name, email = email)))
    }

    @Transactional
    override fun update(id: String, input: UserInput): UserResult {
        val e = repo.findById(id).orElseThrow { NoSuchElementException("User $id") }
        mapper.merge(e, input)
        return mapper.toResult(repo.save(e))
    }

    @Transactional
    override fun delete(id: String) {
        if (!repo.existsById(id)) throw NoSuchElementException("User $id")
        repo.deleteById(id)
    }
}

@Service
class CategoryServiceImpl(
    private val repo: CategoryRepository,
    private val mapper: CategoryMapper
) : CategoryService {
    override fun list() = repo.findAll().map(mapper::toResult)
    override fun get(id: String) = mapper.toResult(repo.findById(id).orElseThrow { NoSuchElementException("Category $id") })

    @Transactional
    override fun create(input: CategoryInput): CategoryResult {
        val name = input.name?.trim() ?: throw IllegalArgumentException("name is required")
        if (repo.existsByNameIgnoreCase(name)) throw IllegalArgumentException("category already exists")
        val id = UUID.randomUUID().toString()
        return mapper.toResult(repo.save(CategoryEntity(id = id, name = name)))
    }

    @Transactional
    override fun update(id: String, input: CategoryInput): CategoryResult {
        val e = repo.findById(id).orElseThrow { NoSuchElementException("Category $id") }
        input.name?.let { if (it != e.name && repo.existsByNameIgnoreCase(it)) throw IllegalArgumentException("category already exists") }
        mapper.merge(e, input)
        return mapper.toResult(repo.save(e))
    }

    @Transactional
    override fun delete(id: String) {
        if (!repo.existsById(id)) throw NoSuchElementException("Category $id")
        repo.deleteById(id)
    }
}

@Service
class StoreServiceImpl(
    private val repo: StoreRepository,
    private val mapper: StoreMapper
) : StoreService {
    override fun list() = repo.findAll().map(mapper::toResult)
    override fun get(id: String) = mapper.toResult(repo.findById(id).orElseThrow { NoSuchElementException("Store $id") })

    @Transactional
    override fun create(input: StoreInput): StoreResult {
        val name = input.name?.trim() ?: throw IllegalArgumentException("name is required")
        val id = UUID.randomUUID().toString()
        return mapper.toResult(repo.save(StoreEntity(id = id, name = name,
            address = input.address?.trim(), district = input.district?.trim(), city = input.city?.trim())))
    }

    @Transactional
    override fun update(id: String, input: StoreInput): StoreResult {
        val e = repo.findById(id).orElseThrow { NoSuchElementException("Store $id") }
        mapper.merge(e, input)
        return mapper.toResult(repo.save(e))
    }

    @Transactional
    override fun delete(id: String) {
        if (!repo.existsById(id)) throw NoSuchElementException("Store $id")
        repo.deleteById(id)
    }
}

@Service
class ProductServiceImpl(
    private val products: ProductRepository,
    private val users: UserRepository,
    private val categories: CategoryRepository,
    private val stores: StoreRepository,
    private val mapper: ProductMapper
) : ProductService {
    override fun list(userId: String?) = when {
        userId.isNullOrBlank() -> products.findAll().map(mapper::toResult)
        else -> products.findAllByUser_Id(userId).map(mapper::toResult)
    }

    override fun get(id: String) = mapper.toResult(products.findById(id).orElseThrow { NoSuchElementException("Product $id") })

    @Transactional
    override fun create(input: ProductInput): ProductResult {
        val user = users.findById(input.userId ?: error("userId is required")).orElseThrow { NoSuchElementException("User ${input.userId}") }
        val category = categories.findById(input.categoryId ?: error("categoryId is required")).orElseThrow { NoSuchElementException("Category ${input.categoryId}") }
        val name = input.name?.trim() ?: throw IllegalArgumentException("name is required")
        val id = UUID.randomUUID().toString()
        val entity = ProductEntity(
            id = id,
            user = user,
            category = category,
            name = name,
            brand = input.brand?.trim(),
            quantity = input.quantity ?: 0,
            minStock = input.minStock ?: 0,
            acquisitionDate = input.acquisitionDate,
            expiryDate = input.expiryDate,
            price = input.price,
            purchaseLocation = input.purchaseLocationId?.let { stores.findById(it).orElse(null) },
            imageUrl = input.imageUrl?.trim()
        )
        return mapper.toResult(products.save(entity))
    }

    @Transactional
    override fun update(id: String, input: ProductInput): ProductResult {
        val e = products.findById(id).orElseThrow { NoSuchElementException("Product $id") }
        input.userId?.let { e.user = users.findById(it).orElseThrow { NoSuchElementException("User $it") } }
        input.categoryId?.let { e.category = categories.findById(it).orElseThrow { NoSuchElementException("Category $it") } }
        input.purchaseLocationId?.let { e.purchaseLocation = stores.findById(it).orElse(null) }
        mapper.merge(e, input)
        return mapper.toResult(products.save(e))
    }

    @Transactional
    override fun delete(id: String) {
        if (!products.existsById(id)) throw NoSuchElementException("Product $id")
        products.deleteById(id)
    }
}

@Service
class MovementServiceImpl(
    private val movements: MovementRepository,
    private val products: ProductRepository,
    private val users: UserRepository,
    private val stores: StoreRepository,
    private val mapper: MovementMapper
) : MovementService {
    override fun list(userId: String?) = when {
        userId.isNullOrBlank() -> movements.findAll().map(mapper::toResult)
        else -> movements.findAllByUser_Id(userId).map(mapper::toResult)
    }

    override fun get(id: String) = mapper.toResult(movements.findById(id).orElseThrow { NoSuchElementException("Movement $id") })

    @Transactional
    override fun create(input: MovementInput): MovementResult {
        val product = products.findById(input.productId ?: error("productId is required")).orElseThrow { NoSuchElementException("Product ${input.productId}") }
        val user = users.findById(input.userId ?: error("userId is required")).orElseThrow { NoSuchElementException("User ${input.userId}") }
        val type = input.type ?: throw IllegalArgumentException("type is required")
        val qty = input.quantity ?: throw IllegalArgumentException("quantity is required")
        val store = input.storeId?.let { stores.findById(it).orElse(null) }

        // Reglas del dominio
        when (type) {
            MovementType.PURCHASE -> {
                require(qty > 0) { "quantity must be > 0 for PURCHASE" }
                require(input.unitPrice != null) { "unitPrice required for PURCHASE" }
                product.quantity += qty
            }
            MovementType.CONSUMPTION -> {
                require(qty < 0) { "quantity must be < 0 for CONSUMPTION" }
                product.quantity += qty
            }
            MovementType.ADJUSTMENT -> {
                // Puede ser positivo o negativo
                product.quantity += qty
            }
        }

        val entity = MovementEntity(
            id = UUID.randomUUID().toString(),
            product = product,
            user = user,
            store = store,
            type = type,
            quantity = qty,
            unitPrice = input.unitPrice,
            note = input.note?.trim(),
            occurredAt = input.occurredAt ?: Date(),
        )
        // Guardar movimiento y producto actualizado en la misma transacción
        return mapper.toResult(movements.save(entity))
    }

    @Transactional
    override fun delete(id: String) {
        // Nota: eliminar movimientos NO revierte stock (decisión de negocio).
        if (!movements.existsById(id)) throw NoSuchElementException("Movement $id")
        movements.deleteById(id)
    }
}

@Service
class ShoppingItemServiceImpl(
    private val items: ShoppingItemRepository,
    private val products: ProductRepository,
    private val users: UserRepository,
    private val stores: StoreRepository,
    private val mapper: ShoppingItemMapper
) : ShoppingItemService {
    override fun list(userId: String?, purchased: Boolean?) = when {
        !userId.isNullOrBlank() && purchased != null -> items.findAllByUser_IdAndIsPurchased(userId, purchased).map(mapper::toResult)
        !userId.isNullOrBlank() -> items.findAllByUser_Id(userId).map(mapper::toResult)
        else -> items.findAll().map(mapper::toResult)
    }

    override fun get(id: String) = mapper.toResult(items.findById(id).orElseThrow { NoSuchElementException("ShoppingItem $id") })

    @Transactional
    override fun create(input: ShoppingItemInput): ShoppingItemResult {
        val user = users.findById(input.userId ?: error("userId is required")).orElseThrow { NoSuchElementException("User ${input.userId}") }
        val product = products.findById(input.productId ?: error("productId is required")).orElseThrow { NoSuchElementException("Product ${input.productId}") }
        val entity = ShoppingItemEntity(
            id = UUID.randomUUID().toString(),
            user = user,
            product = product,
            desiredQuantity = input.desiredQuantity ?: 1,
            isPurchased = input.isPurchased ?: false,
            targetStore = input.targetStoreId?.let { stores.findById(it).orElse(null) },
            purchasedAt = input.purchasedAt
        )
        return mapper.toResult(items.save(entity))
    }

    @Transactional
    override fun update(id: String, input: ShoppingItemInput): ShoppingItemResult {
        val e = items.findById(id).orElseThrow { NoSuchElementException("ShoppingItem $id") }
        input.userId?.let { e.user = users.findById(it).orElseThrow { NoSuchElementException("User $it") } }
        input.productId?.let { e.product = products.findById(it).orElseThrow { NoSuchElementException("Product $it") } }
        input.targetStoreId?.let { e.targetStore = stores.findById(it).orElse(null) }
        mapper.merge(e, input)
        return mapper.toResult(items.save(e))
    }

    @Transactional
    override fun delete(id: String) {
        if (!items.existsById(id)) throw NoSuchElementException("ShoppingItem $id")
        items.deleteById(id)
    }
}

@Service
class AlertServiceImpl(
    private val alerts: AlertRepository,
    private val users: UserRepository,
    private val products: ProductRepository,
    private val mapper: AlertMapper
) : AlertService {
    override fun list(userId: String?) =
        alerts.findAll().map(mapper::toResult).filter { userId.isNullOrBlank() || it.userId == userId }

    override fun get(id: String) = mapper.toResult(alerts.findById(id).orElseThrow { NoSuchElementException("Alert $id") })

    @Transactional
    override fun create(input: AlertInput): AlertResult {
        val user = users.findById(input.userId ?: error("userId is required")).orElseThrow { NoSuchElementException("User ${input.userId}") }
        val product = products.findById(input.productId ?: error("productId is required")).orElseThrow { NoSuchElementException("Product ${input.productId}") }
        val entity = AlertEntity(
            id = UUID.randomUUID().toString(),
            user = user,
            product = product,
            type = input.type ?: throw IllegalArgumentException("type is required"),
            triggerAt = input.triggerAt ?: Date(),
            isActive = input.isActive ?: true,
            resolvedAt = input.resolvedAt
        )
        return mapper.toResult(alerts.save(entity))
    }

    @Transactional
    override fun update(id: String, input: AlertInput): AlertResult {
        val e = alerts.findById(id).orElseThrow { NoSuchElementException("Alert $id") }
        input.userId?.let { e.user = users.findById(it).orElseThrow { NoSuchElementException("User $it") } }
        input.productId?.let { e.product = products.findById(it).orElseThrow { NoSuchElementException("Product $it") } }
        mapper.merge(e, input)
        return mapper.toResult(alerts.save(e))
    }

    @Transactional
    override fun delete(id: String) {
        if (!alerts.existsById(id)) throw NoSuchElementException("Alert $id")
        alerts.deleteById(id)
    }
}

@Service
class PriceHistoryServiceImpl(
    private val repo: PriceHistoryRepository,
    private val products: ProductRepository,
    private val stores: StoreRepository,
    private val mapper: PriceHistoryMapper
) : PriceHistoryService {
    override fun list(productId: String?, storeId: String?) =
        repo.findAll().map(mapper::toResult).filter {
            (productId == null || it.productId == productId) && (storeId == null || it.storeId == storeId)
        }

    override fun get(id: String) = mapper.toResult(repo.findById(id).orElseThrow { NoSuchElementException("PriceHistory $id") })

    @Transactional
    override fun create(input: PriceHistoryInput): PriceHistoryResult {
        val product = products.findById(input.productId ?: error("productId is required")).orElseThrow { NoSuchElementException("Product ${input.productId}") }
        val store = stores.findById(input.storeId ?: error("storeId is required")).orElseThrow { NoSuchElementException("Store ${input.storeId}") }
        val e = PriceHistoryEntity(
            id = UUID.randomUUID().toString(),
            product = product,
            store = store,
            unitPrice = input.unitPrice ?: throw IllegalArgumentException("unitPrice is required"),
            recordedAt = input.recordedAt ?: Date()
        )
        return mapper.toResult(repo.save(e))
    }
}

@Service
class ProductRatingServiceImpl(
    private val repo: ProductRatingRepository,
    private val users: UserRepository,
    private val products: ProductRepository,
    private val mapper: ProductRatingMapper
) : ProductRatingService {
    override fun list(userId: String?, productId: String?) =
        repo.findAll().map(mapper::toResult).filter {
            (userId == null || it.userId == userId) && (productId == null || it.productId == productId)
        }

    override fun get(id: String) = mapper.toResult(repo.findById(id).orElseThrow { NoSuchElementException("ProductRating $id") })

    @Transactional
    override fun upsert(input: ProductRatingInput): ProductRatingResult {
        val user = users.findById(input.userId ?: error("userId is required")).orElseThrow { NoSuchElementException("User ${input.userId}") }
        val product = products.findById(input.productId ?: error("productId is required")).orElseThrow { NoSuchElementException("Product ${input.productId}") }
        val existing = repo.findByUser_IdAndProduct_Id(user.id, product.id).orElse(null)
        val entity = existing ?: ProductRatingEntity(
            id = UUID.randomUUID().toString(),
            user = user,
            product = product,
            qualityScore = input.qualityScore ?: throw IllegalArgumentException("qualityScore is required"),
            notes = input.notes?.trim(),
            createdAt = input.createdAt ?: Date()
        )
        if (existing != null) {
            input.qualityScore?.let { entity.qualityScore = it }
            entity.notes = input.notes?.trim() ?: entity.notes
        }
        return mapper.toResult(repo.save(entity))
    }

    @Transactional
    override fun delete(id: String) {
        if (!repo.existsById(id)) throw NoSuchElementException("ProductRating $id")
        repo.deleteById(id)
    }
}
