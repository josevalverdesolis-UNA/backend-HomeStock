package cr.ac.una.homestock.service

import cr.ac.una.homestock.domain.entity.*
import cr.ac.una.homestock.dto.*
import cr.ac.una.homestock.mapper.*
import cr.ac.una.homestock.repository.*
import cr.ac.una.homestock.common.BusinessException
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.PageRequest
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
            repo.existsByNameIgnoreCaseAndLocationIgnoreCase(input.name, input.location)
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
            repo.existsByNameIgnoreCaseAndLocationIgnoreCase(newName, newLoc)
        val isSameAsCurrent = newName.equals(entity.name, ignoreCase = true) &&
                ((newLoc == null && entity.location == null) || (newLoc != null && newLoc.equals(entity.location, ignoreCase = true)))
        if (conflict && !isSameAsCurrent) {
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

    fun search(
        userId: Long,
        q: String?,
        categoryId: Long?,
        minStockOnly: Boolean,
        page: Int,
        size: Int
    ): PageResponse<ProductResult> {
        val p = page.coerceAtLeast(0)
        val s = size.coerceIn(1, 200)
        val pageable = PageRequest.of(p, s)
        val pageRes = repo.search(userId, q?.takeIf { it.isNotBlank() }, categoryId, minStockOnly, pageable)
        return PageResponse(
            content = pageRes.content.map(mapper::toResult),
            page = pageRes.number,
            size = pageRes.size,
            totalElements = pageRes.totalElements,
            totalPages = pageRes.totalPages
        )
    }

    fun getById(id: Long): ProductResult {
        val entity = repo.findById(id).orElseThrow { notFound("Product", id) }
        return mapper.toResult(entity)
    }

    fun findByBarcode(userId: Long, code: String): ProductResult? =
        repo.findFirstByUser_IdAndBarcode(userId, code).map(mapper::toResult).orElse(null)

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
        // Coherencia: movement debe ser del mismo usuario que el dueño del producto
        val productUserId = product.user?.id ?: throw IllegalStateException("Product without owner user")
        if (productUserId != input.userId) {
            throw BusinessException("Movement userId does not match product owner", HttpStatus.FORBIDDEN)
        }
        // Reglas
        if (input.quantity == 0) throw BusinessException("quantity must not be zero")
        if (input.type == MovementTypeDTO.PURCHASE && (input.unitPrice == null || input.unitPrice <= BigDecimal.ZERO)) {
            throw BusinessException("unitPrice is required and must be > 0 for PURCHASE")
        }
        // Normalización: almacenar cantidad positiva, aplicar signo al stock
        val absQty = abs(input.quantity)
        val stockDelta = when (input.type) {
            MovementTypeDTO.PURCHASE -> absQty
            MovementTypeDTO.CONSUMPTION -> -absQty
            MovementTypeDTO.ADJUSTMENT -> if (input.quantity >= 0) absQty else -absQty
        }
        // Crear movement con occurredAt por defecto si no viene
        val entity = mapper.fromCreate(input.copy(quantity = absQty, occurredAt = input.occurredAt ?: Instant.now()))
        // Forzar coherencia de referencias
        entity.user = product.user
        entity.product = product

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
        val product = productRepo.findById(input.productId).orElseThrow { notFound("Product", input.productId) }
        // Coherencia: el producto debe pertenecer al userId de la solicitud
        val ownerId = product.user?.id ?: throw IllegalStateException("Product without owner user")
        if (ownerId != input.userId) throw BusinessException("Product does not belong to user", HttpStatus.FORBIDDEN)
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
    private val productRepo: ProductRepository,
) {
    fun listActive(userId: Long): List<AlertResult> =
        repo.findAllByUser_IdAndActiveTrue(userId).map(mapper::toResult)

    @Transactional
    fun create(input: AlertCreate): AlertResult {
        // Si trae productId, validar que ese producto pertenezca al userId
        input.productId?.let { pid ->
            val product = productRepo.findById(pid).orElseThrow { notFound("Product", pid) }
            val ownerId = product.user?.id ?: throw IllegalStateException("Product without owner user")
            if (ownerId != input.userId) throw BusinessException("Product does not belong to user", HttpStatus.FORBIDDEN)
        }
        val entity = mapper.fromCreate(input.copy(triggerAt = input.triggerAt ?: Instant.now()))
        return mapper.toResult(repo.save(entity))
    }

    @Transactional
    fun update(id: Long, input: AlertUpdate): AlertResult {
        val entity = repo.findById(id).orElseThrow { notFound("Alert", id) }
        val wasActive = entity.active
        mapper.update(input, entity)
        // Si se cambió de activo a inactivo, resolvemos ahora
        if (wasActive && entity.active == false) {
            entity.resolvedAt = Instant.now()
        }
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

// ------------------------------
// ShoppingList (listas agrupadas)
// ------------------------------

@Service
class ShoppingListService(
    private val listRepo: ShoppingListRepository,
    private val itemRepo: ShoppingListItemRepository,
    private val productRepo: ProductRepository,
    private val userRepo: UserRepository,
    private val listMapper: ShoppingListMapper,
    private val itemMapper: ShoppingListItemMapper,
    private val movementService: MovementService,
) {
    fun list(userId: Long): List<ShoppingListResult> =
        listRepo.findAllByUser_Id(userId).map(listMapper::toResult)

    fun detail(id: Long): ShoppingListDetailResult {
        val entity = listRepo.findById(id).orElseThrow { notFound("ShoppingList", id) }
        val items = itemRepo.findAllByList_Id(id).map(itemMapper::toResult)
        return listMapper.toDetail(entity, items)
    }

    @Transactional
    fun create(input: ShoppingListCreate): ShoppingListResult {
        if (!userRepo.existsById(input.userId)) notFound("User", input.userId)
        val entity = listMapper.fromCreate(input)
        return listMapper.toResult(listRepo.save(entity))
    }

    @Transactional
    fun addItem(listId: Long, input: ShoppingListItemCreate): ShoppingListItemResult {
        val list = listRepo.findById(listId).orElseThrow { notFound("ShoppingList", listId) }
        if (list.status != ShoppingListStatus.DRAFT) throw BusinessException("List is not editable", HttpStatus.CONFLICT)
        val product = productRepo.findById(input.productId).orElseThrow { notFound("Product", input.productId) }
        // Coherencia: el producto debe pertenecer al mismo usuario de la lista
        val listUserId = list.user?.id ?: throw IllegalStateException("List without owner user")
        val productUserId = product.user?.id ?: throw IllegalStateException("Product without owner user")
        if (listUserId != productUserId) throw BusinessException("Product does not belong to list user", HttpStatus.FORBIDDEN)

        val existing = itemRepo.findByList_IdAndProduct_Id(listId, input.productId)
        val entity = if (existing.isPresent) {
            val e = existing.get()
            e.desiredQuantity += input.desiredQuantity
            input.targetStoreId?.let { e.targetStore = Store(id = it) }
            e
        } else {
            itemMapper.fromCreate(list, input)
        }
        return itemMapper.toResult(itemRepo.save(entity))
    }

    @Transactional
    fun updateItem(listId: Long, itemId: Long, input: ShoppingListItemUpdate): ShoppingListItemResult {
        val entity = itemRepo.findByList_IdAndId(listId, itemId).orElseThrow { notFound("ShoppingListItem", itemId) }
        val list = entity.list ?: throw IllegalStateException("Item without list")
        if (list.status != ShoppingListStatus.DRAFT) throw BusinessException("List is not editable", HttpStatus.CONFLICT)
        val prevChecked = entity.checked
        itemMapper.update(input, entity)
        // Marcar timestamps
        input.checked?.let { chk ->
            entity.checkedAt = if (chk) Instant.now() else null
        }
        // Evitar no-ops que dejen estado inconsistente
        if (!prevChecked && entity.checked && entity.checkedAt == null) entity.checkedAt = Instant.now()
        if (!entity.checked) entity.checkedAt = null
        return itemMapper.toResult(entity)
    }

    @Transactional
    fun generateFromLowStock(listId: Long): ShoppingListDetailResult {
        val list = listRepo.findById(listId).orElseThrow { notFound("ShoppingList", listId) }
        if (list.status != ShoppingListStatus.DRAFT) throw BusinessException("List is not editable", HttpStatus.CONFLICT)
        val userId = list.user?.id ?: throw IllegalStateException("List without owner user")
        val today = java.time.LocalDate.now()
        val products = productRepo.findAllByUser_Id(userId)
        products.forEach { p ->
            val lowStock = p.quantity <= p.minStock
            val expiring = p.expiryDate?.let { !it.isAfter(today) } ?: false
            if (lowStock || expiring) {
                val desired = if (lowStock) (p.minStock - p.quantity).coerceAtLeast(1) else 1
                val existing = itemRepo.findByList_IdAndProduct_Id(listId, p.id!!)
                if (existing.isPresent) {
                    val it = existing.get()
                    it.desiredQuantity = maxOf(it.desiredQuantity, desired)
                } else {
                    val toCreate = ShoppingListItem(
                        list = list,
                        product = p,
                        desiredQuantity = desired,
                        checked = false,
                        checkedAt = null,
                        targetStore = p.purchaseLocation
                    )
                    itemRepo.save(toCreate)
                }
            }
        }
        // devolver detalle
        val items = itemRepo.findAllByList_Id(listId).map(itemMapper::toResult)
        return listMapper.toDetail(list, items)
    }

    @Transactional
    fun toPurchase(listId: Long): ShoppingListDetailResult {
        val list = listRepo.findById(listId).orElseThrow { notFound("ShoppingList", listId) }
        if (list.status != ShoppingListStatus.DRAFT) throw BusinessException("List cannot be converted (status=${'$'}{list.status})", HttpStatus.CONFLICT)
        val userId = list.user?.id ?: throw IllegalStateException("List without owner user")
        val allItems = itemRepo.findAllByList_Id(listId)
        val selected = allItems.filter { it.checked }.ifEmpty { allItems }
        // Convertir a movimientos tipo ADJUSTMENT +
        selected.forEach { it ->
            val productId = it.product?.id ?: return@forEach
            movementService.create(
                MovementCreate(
                    userId = userId,
                    productId = productId,
                    type = MovementTypeDTO.ADJUSTMENT,
                    quantity = it.desiredQuantity,
                    unitPrice = null,
                    storeId = it.targetStore?.id,
                    note = "Auto from shopping list ${'$'}{list.id}",
                    occurredAt = Instant.now()
                )
            )
        }
        // marcar lista como completada
        list.status = ShoppingListStatus.COMPLETED
        // devolver detalle actualizado
        val items = itemRepo.findAllByList_Id(listId).map(itemMapper::toResult)
        return listMapper.toDetail(list, items)
    }
}

// Comentario: Ajustado import de BusinessException (ahora en common) y uso de abs().
