package cr.ac.una.homestock.service

import cr.ac.una.homestock.domain.entity.*
import cr.ac.una.homestock.dto.*
import cr.ac.una.homestock.mapper.*
import cr.ac.una.homestock.repository.*
import cr.ac.una.homestock.web.BusinessException
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import kotlin.math.abs

// Alias para enums DTO vs Entity
import cr.ac.una.homestock.dto.MovementType as MovementTypeDTO
import cr.ac.una.homestock.domain.entity.ShoppingSource as ShoppingSourceEntity
import cr.ac.una.homestock.domain.entity.AlertType as AlertTypeEntity

// Utilidades locales
private fun notFound(entity: String, id: Any): Nothing = throw EntityNotFoundException("$entity not found: $id")

// ------------------------------
// Category
// ------------------------------

@Service
class CategoryService(
    private val repo: CategoryRepository,
    private val mapper: CategoryMapper,
) {
    fun list(): List<CategoryResult> = repo.findAll().map(mapper::toResult)

    @Transactional
    fun create(input: CategoryCreate): CategoryResult {
        if (repo.existsByNameIgnoreCase(input.name)) {
            throw BusinessException("Category name already exists", HttpStatus.CONFLICT)
        }
        val entity = mapper.toEntity(input)
        return mapper.toResult(repo.save(entity))
    }

    @Transactional
    fun update(id: Long, input: CategoryUpdate): CategoryResult {
        val entity = repo.findById(id).orElseThrow { notFound("Category", id) }
        mapper.update(input, entity)
        return mapper.toResult(entity)
    }

    @Transactional
    fun delete(id: Long) {
        if (!repo.existsById(id)) notFound("Category", id)
        repo.deleteById(id)
    }
}

// ------------------------------
// Store
// ------------------------------

@Service
class StoreService(
    private val repo: StoreRepository,
    private val mapper: StoreMapper,
) {
    fun list(): List<StoreResult> = repo.findAll().map(mapper::toResult)

    @Transactional
    fun create(input: StoreCreate): StoreResult {
        val conflict = if (input.location == null)
            repo.existsByNameIgnoreCaseAndLocationIsNull(input.name)
        else
            repo.existsByNameIgnoreCaseAndLocationIgnoreCase(input.name, input.location!!)
        if (conflict) throw BusinessException("Store (name, location) already exists", HttpStatus.CONFLICT)
        val entity = mapper.toEntity(input)
        return mapper.toResult(repo.save(entity))
    }

    @Transactional
    fun update(id: Long, input: StoreUpdate): StoreResult {
        val entity = repo.findById(id).orElseThrow { notFound("Store", id) }
        // Validar conflicto si cambian name/location
        val newName = input.name ?: entity.name
        val newLoc = input.location ?: entity.location
        val conflict = if (newLoc == null)
            repo.existsByNameIgnoreCaseAndLocationIsNull(newName)
        else
            repo.existsByNameIgnoreCaseAndLocationIgnoreCase(newName, newLoc!!)
        if (conflict && !(newName.equals(entity.name, true) && (newLoc?.equals(entity.location, true) ?: entity.location == null))) {
            throw BusinessException("Store (name, location) already exists", HttpStatus.CONFLICT)
        }
        mapper.update(input, entity)
        return mapper.toResult(entity)
    }

    @Transactional
    fun delete(id: Long) {
        if (!repo.existsById(id)) notFound("Store", id)
        repo.deleteById(id)
    }
}

// ------------------------------
// Product
// ------------------------------

@Service
class ProductService(
    private val repo: ProductRepository,
    private val userRepo: UserRepository,
    private val categoryRepo: CategoryRepository,
    private val storeRepo: StoreRepository,
    private val mapper: ProductMapper,
) {
    fun byUser(userId: Long): List<ProductResult> = repo.findAllByUser_Id(userId).map(mapper::toResult)

    @Transactional
    fun create(input: ProductCreate): ProductResult {
        if (!userRepo.existsById(input.userId)) notFound("User", input.userId)
        if (!categoryRepo.existsById(input.categoryId)) notFound("Category", input.categoryId)
        if (input.purchaseLocationId != null && !storeRepo.existsById(input.purchaseLocationId)) notFound("Store", input.purchaseLocationId)
        if (repo.existsByUser_IdAndNameIgnoreCase(input.userId, input.name)) {
            throw BusinessException("Product name already exists for this user", HttpStatus.CONFLICT)
        }
        val entity = mapper.fromCreate(input)
        return mapper.toResult(repo.save(entity))
    }

    @Transactional
    fun update(id: Long, input: ProductUpdate): ProductResult {
        val entity = repo.findById(id).orElseThrow { notFound("Product", id) }
        // Validaciones de referencias si se envían
        input.categoryId?.let { if (!categoryRepo.existsById(it)) notFound("Category", it) }
        input.purchaseLocationId?.let { if (!storeRepo.existsById(it)) notFound("Store", it) }
        mapper.update(input, entity)
        return mapper.toResult(entity)
    }

    fun get(userId: Long, id: Long): ProductResult {
        val entity = repo.findByUser_IdAndId(userId, id).orElseThrow { notFound("Product", id) }
        return mapper.toResult(entity)
    }

    @Transactional
    fun delete(id: Long) {
        if (!repo.existsById(id)) notFound("Product", id)
        repo.deleteById(id)
    }
}

// ------------------------------
// Movement (impacta stock + reglas)
// ------------------------------

@Service
class MovementService(
    private val repo: MovementRepository,
    private val productRepo: ProductRepository,
    private val shoppingRepo: ShoppingItemRepository,
    private val alertRepo: AlertRepository,
    private val mapper: MovementMapper,
) {
    @Transactional
    fun create(input: MovementCreate): MovementResult {
        val product = productRepo.findById(input.productId).orElseThrow { notFound("Product", input.productId) }
        // Reglas
        if (input.quantity == 0) throw BusinessException("quantity must not be zero")
        if (input.type == MovementTypeDTO.PURCHASE && (input.unitPrice == null || input.unitPrice <= BigDecimal.ZERO)) {
            throw BusinessException("unitPrice is required and must be > 0 for PURCHASE")
        }
        // Normalización: almacenar cantidad positiva, aplicar signo al stock
        val absQty = kotlin.math.abs(input.quantity)
        val stockDelta = when (input.type) {
            MovementTypeDTO.PURCHASE -> absQty
            MovementTypeDTO.CONSUMPTION -> -absQty
            MovementTypeDTO.ADJUSTMENT -> if (input.quantity >= 0) absQty else -absQty
        }
        // Crear movement con occurredAt por defecto si no viene
        val entity = mapper.fromCreate(input.copy(quantity = absQty, occurredAt = input.occurredAt ?: Instant.now()))
        val saved = repo.save(entity)

        // Impacto de stock
        product.quantity += stockDelta
        if (product.quantity < 0) throw BusinessException("Stock cannot be negative after movement")

        // Auto-regla: crear/actualizar ShoppingItem si bajo stock
        if (product.quantity <= product.minStock) upsertAutoShoppingItem(product)

        // Alertas LOW_STOCK activas
        manageLowStockAlert(product)

        return mapper.toResult(saved)
    }

    private fun upsertAutoShoppingItem(product: Product) {
        val userId = product.user?.id ?: return
        val existing = shoppingRepo.findByUser_IdAndProduct_IdAndPurchasedFalse(userId, product.id!!)
        val needed = (product.minStock - product.quantity).coerceAtLeast(1)
        if (existing.isPresent) {
            val it = existing.get()
            it.desiredQuantity = needed
            it.source = ShoppingSourceEntity.AUTO_RULE
        } else {
            val si = ShoppingItem(
                user = product.user,
                product = product,
                desiredQuantity = needed,
                purchased = false,
                purchasedAt = null,
                source = ShoppingSourceEntity.AUTO_RULE,
                targetStore = product.purchaseLocation
            )
            shoppingRepo.save(si)
        }
    }

    private fun manageLowStockAlert(product: Product) {
        val userId = product.user?.id ?: return
        val active = alertRepo.findAllByUser_IdAndProduct_IdAndActiveTrue(userId, product.id!!)
        val below = product.quantity <= product.minStock
        if (below && active.isEmpty()) {
            alertRepo.save(
                Alert(
                    user = product.user,
                    product = product,
                    type = AlertTypeEntity.LOW_STOCK,
                    message = "Stock below minimum for ${product.name}",
                    triggerAt = Instant.now(),
                    active = true
                )
            )
        } else if (!below && active.isNotEmpty()) {
            active.forEach { it.active = false; it.resolvedAt = Instant.now() }
        }
    }
}

// ------------------------------
// ShoppingItem
// ------------------------------

@Service
class ShoppingItemService(
    private val repo: ShoppingItemRepository,
    private val mapper: ShoppingItemMapper,
    private val productRepo: ProductRepository,
    private val userRepo: UserRepository,
) {
    fun listPending(userId: Long): List<ShoppingItemResult> =
        repo.findAllByUser_IdAndPurchasedFalse(userId).map(mapper::toResult)

    @Transactional
    fun create(input: ShoppingItemCreate): ShoppingItemResult {
        if (!userRepo.existsById(input.userId)) notFound("User", input.userId)
        if (!productRepo.existsById(input.productId)) notFound("Product", input.productId)
        val exists = repo.existsByUser_IdAndProduct_IdAndPurchasedFalse(input.userId, input.productId)
        if (exists) throw BusinessException("Shopping item already exists (pending)", HttpStatus.CONFLICT)
        val entity = mapper.fromCreate(input)
        return mapper.toResult(repo.save(entity))
    }

    @Transactional
    fun update(id: Long, input: ShoppingItemUpdate): ShoppingItemResult {
        val entity = repo.findById(id).orElseThrow { notFound("ShoppingItem", id) }
        mapper.update(input, entity)
        return mapper.toResult(entity)
    }
}

// ------------------------------
// Alert
// ------------------------------

@Service
class AlertService(
    private val repo: AlertRepository,
    private val mapper: AlertMapper,
) {
    fun listActive(userId: Long): List<AlertResult> =
        repo.findAllByUser_IdAndActiveTrue(userId).map(mapper::toResult)

    @Transactional
    fun create(input: AlertCreate): AlertResult {
        val entity = mapper.fromCreate(input.copy(triggerAt = input.triggerAt ?: Instant.now()))
        return mapper.toResult(repo.save(entity))
    }

    @Transactional
    fun update(id: Long, input: AlertUpdate): AlertResult {
        val entity = repo.findById(id).orElseThrow { notFound("Alert", id) }
        mapper.update(input, entity)
        return mapper.toResult(entity)
    }

    @Transactional
    fun close(id: Long): AlertResult {
        val entity = repo.findById(id).orElseThrow { notFound("Alert", id) }
        entity.active = false
        entity.resolvedAt = Instant.now()
        return mapper.toResult(entity)
    }
}

// ------------------------------
// PriceHistory
// ------------------------------

@Service
class PriceHistoryService(
    private val repo: PriceHistoryRepository,
    private val productRepo: ProductRepository,
    private val mapper: PriceHistoryMapper,
) {
    @Transactional
    fun add(input: PriceHistoryCreate): PriceHistoryResult {
        productRepo.findById(input.productId).orElseThrow { notFound("Product", input.productId) }
        val entity = mapper.fromCreate(input.copy(recordedAt = input.recordedAt ?: Instant.now()))
        return mapper.toResult(repo.save(entity))
    }

    fun lastForProduct(productId: Long): PriceHistoryResult? =
        repo.findTop1ByProduct_IdOrderByRecordedAtDesc(productId).map(mapper::toResult).orElse(null)
}

// ------------------------------
// ProductRating (upsert por (user, product))
// ------------------------------

@Service
class ProductRatingService(
    private val repo: ProductRatingRepository,
    private val mapper: ProductRatingMapper,
    private val userRepo: UserRepository,
    private val productRepo: ProductRepository,
) {
    @Transactional
    fun upsert(input: ProductRatingCreate): ProductRatingResult {
        if (!userRepo.existsById(input.userId)) notFound("User", input.userId)
        if (!productRepo.existsById(input.productId)) notFound("Product", input.productId)
        val existing = repo.findByUser_IdAndProduct_Id(input.userId, input.productId)
        val entity = if (existing.isPresent) {
            val e = existing.get()
            e.qualityScore = input.qualityScore
            e.notes = input.notes
            e
        } else {
            mapper.fromCreate(input)
        }
        return mapper.toResult(repo.save(entity))
    }
}

// Comentario de cambios: Movement guarda quantity positiva y occurredAt; ShoppingItem usa desiredQuantity y targetStore; StoreService valida UQ (name, location); AlertService close()
