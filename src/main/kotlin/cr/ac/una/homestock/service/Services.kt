package cr.una.homestock.domain.service

import cr.una.homestock.data.repository.*
import cr.una.homestock.domain.model.*
import cr.una.homestock.domain.mapper.*
import cr.una.homestock.web.dto.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/* ======= USER ======= */
interface UserService {
    fun list(): List<UserResult>
    fun get(id: String): UserResult
    fun create(input: UserInput): IdResponse
    fun update(id: String, update: UserUpdate): UserResult
    fun delete(id: String)
}

@Service
class UserServiceImpl(
    private val repo: UserRepository,
    private val mapper: UserMapper
) : UserService {
    override fun list() = mapper.toResults(repo.findAll())
    override fun get(id: String) = mapper.toResult(repo.findById(id).orElseThrow())
    override fun create(input: UserInput): IdResponse {
        val entity = mapper.fromInput(input)
        return IdResponse(repo.save(entity).id!!)
    }
    override fun update(id: String, update: UserUpdate): UserResult {
        val e = repo.findById(id).orElseThrow()
        mapper.merge(e, update)
        return mapper.toResult(repo.save(e))
    }
    override fun delete(id: String) = repo.deleteById(id)
}

/* ======= CATEGORY ======= */
interface CategoryService {
    fun list(): List<CategoryResult>
    fun get(id: String): CategoryResult
    fun create(input: CategoryInput): IdResponse
    fun update(id: String, update: CategoryUpdate): CategoryResult
    fun delete(id: String)
}
@Service
class CategoryServiceImpl(
    private val repo: CategoryRepository,
    private val mapper: CategoryMapper
) : CategoryService {
    override fun list() = mapper.toResults(repo.findAll())
    override fun get(id: String) = mapper.toResult(repo.findById(id).orElseThrow())
    override fun create(input: CategoryInput): IdResponse {
        val e = mapper.fromInput(input)
        return IdResponse(repo.save(e).id!!)
    }
    override fun update(id: String, update: CategoryUpdate): CategoryResult {
        val e = repo.findById(id).orElseThrow()
        mapper.merge(e, update)
        return mapper.toResult(repo.save(e))
    }
    override fun delete(id: String) = repo.deleteById(id)
}

/* ======= STORE ======= */
interface StoreService {
    fun list(): List<StoreResult>
    fun get(id: String): StoreResult
    fun create(input: StoreInput): IdResponse
    fun update(id: String, update: StoreUpdate): StoreResult
    fun delete(id: String)
}
@Service
class StoreServiceImpl(
    private val repo: StoreRepository,
    private val mapper: StoreMapper
) : StoreService {
    override fun list() = mapper.toResults(repo.findAll())
    override fun get(id: String) = mapper.toResult(repo.findById(id).orElseThrow())
    override fun create(input: StoreInput): IdResponse {
        val e = mapper.fromInput(input)
        return IdResponse(repo.save(e).id!!)
    }
    override fun update(id: String, update: StoreUpdate): StoreResult {
        val e = repo.findById(id).orElseThrow()
        mapper.merge(e, update)
        return mapper.toResult(repo.save(e))
    }
    override fun delete(id: String) = repo.deleteById(id)
}

/* ======= PRODUCT ======= */
interface ProductService {
    fun listByUser(userId: String): List<ProductResult>
    fun get(id: String): ProductResult
    fun create(input: ProductInput): IdResponse
    fun update(id: String, update: ProductUpdate): ProductResult
    fun delete(id: String)
}

@Service
class ProductServiceImpl(
    private val repo: ProductRepository,
    private val mapper: ProductMapper
) : ProductService {
    override fun listByUser(userId: String) = mapper.toResults(repo.findByUserId(userId))
    override fun get(id: String) = mapper.toResult(repo.findById(id).orElseThrow())
    override fun create(input: ProductInput): IdResponse {
        val e = mapper.fromInput(input)
        val saved = repo.save(e)
        return IdResponse(saved.id!!)
    }
    override fun update(id: String, update: ProductUpdate): ProductResult {
        val e = repo.findById(id).orElseThrow()
        mapper.merge(e, update)
        e.updatedAt = OffsetDateTime.now()
        return mapper.toResult(repo.save(e))
    }
    override fun delete(id: String) = repo.deleteById(id)
}

/* ======= MOVEMENT ======= */
interface MovementService {
    fun listByProduct(productId: String): List<MovementResult>
    fun get(id: String): MovementResult
    fun create(input: MovementInput): IdResponse
}

@Service
class MovementServiceImpl(
    private val repo: MovementRepository,
    private val productRepo: ProductRepository,
    private val shoppingRepo: ShoppingItemRepository,
    private val alertRepo: AlertRepository,
    private val mapper: MovementMapper
) : MovementService {

    @Transactional
    override fun create(input: MovementInput): IdResponse {
        requireNotNull(input.type)
        requireNotNull(input.quantity)
        // Regla: PURCHASE/ADJUSTMENT+ => quantity > 0; CONSUMPTION/ADJUSTMENT- => quantity < 0 (convención)
        val p = productRepo.findById(input.productId!!).orElseThrow()

        // Validaciones básicas de negocio
        if (input.type == MovementTypeDto.PURCHASE && (input.quantity <= 0)) {
            throw IllegalArgumentException("Purchase requires quantity > 0")
        }
        if (input.type == MovementTypeDto.CONSUMPTION && (input.quantity >= 0)) {
            throw IllegalArgumentException("Consumption requires quantity < 0")
        }
        if (input.type == MovementTypeDto.PURCHASE && input.unitPrice == null) {
            throw IllegalArgumentException("unitPrice is required for PURCHASE")
        }

        // Persistir movimiento
        val entity = mapper.fromInput(input)
        val saved = repo.save(entity)

        // Actualizar stock
        p.quantity += input.quantity
        p.updatedAt = OffsetDateTime.now()
        productRepo.save(p)

        // Regla: LOW_STOCK => crear alerta activa si no existe y generar shopping item AUTO_RULE
        if (p.quantity <= p.minStock) {
            // Shopping item auto si no hay uno abierto
            if (!shoppingRepo.existsByProductIdAndIsPurchased(p.id!!, false)) {
                shoppingRepo.save(
                    ShoppingItem(
                        userId = input.userId!!,
                        productId = p.id!!,
                        desiredQuantity = (p.minStock - p.quantity).coerceAtLeast(1),
                        isPurchased = false,
                        source = ShoppingSource.AUTO_RULE
                    )
                )
            }
            // Alerta de bajo stock
            alertRepo.save(
                Alert(
                    userId = input.userId!!,
                    productId = p.id!!,
                    type = AlertType.LOW_STOCK,
                    triggerAt = OffsetDateTime.now(),
                    isActive = true
                )
            )
        }

        return IdResponse(saved.id!!)
    }

    override fun listByProduct(productId: String) =
        mapper.toResults(repo.findByProductIdOrderByOccurredAtDesc(productId))

    override fun get(id: String) = mapper.toResult(repo.findById(id).orElseThrow())
}

/* ======= SHOPPING ITEM ======= */
interface ShoppingItemService {
    fun listActiveByUser(userId: String): List<ShoppingItemResult>
    fun create(input: ShoppingItemInput): IdResponse
    fun update(id: String, update: ShoppingItemUpdate): ShoppingItemResult
    fun delete(id: String)
}
@Service
class ShoppingItemServiceImpl(
    private val repo: ShoppingItemRepository,
    private val mapper: ShoppingItemMapper
) : ShoppingItemService {
    override fun listActiveByUser(userId: String) =
        mapper.toResults(repo.findByUserIdAndIsPurchased(userId, false))

    override fun create(input: ShoppingItemInput): IdResponse {
        val e = mapper.fromInput(input)
        return IdResponse(repo.save(e).id!!)
    }

    override fun update(id: String, update: ShoppingItemUpdate): ShoppingItemResult {
        val e = repo.findById(id).orElseThrow()
        mapper.merge(e, update)
        if (update.isPurchased == true && e.purchasedAt == null) {
            e.purchasedAt = OffsetDateTime.now()
        }
        return mapper.toResult(repo.save(e))
    }

    override fun delete(id: String) = repo.deleteById(id)
}

/* ======= ALERT ======= */
interface AlertService {
    fun listActiveByUser(userId: String): List<AlertResult>
    fun create(input: AlertInput): IdResponse
    fun update(id: String, update: AlertUpdate): AlertResult
}
@Service
class AlertServiceImpl(
    private val repo: AlertRepository,
    private val mapper: AlertMapper
) : AlertService {
    override fun listActiveByUser(userId: String) =
        mapper.toResults(repo.findByUserIdAndIsActive(userId, true))

    override fun create(input: AlertInput): IdResponse {
        val e = mapper.fromInput(input)
        return IdResponse(repo.save(e).id!!)
    }

    override fun update(id: String, update: AlertUpdate): AlertResult {
        val e = repo.findById(id).orElseThrow()
        mapper.merge(e, update)
        return mapper.toResult(repo.save(e))
    }
}

/* ======= PRICE HISTORY ======= */
interface PriceHistoryService {
    fun list(productId: String, storeId: String): List<PriceHistoryResult>
    fun create(input: PriceHistoryInput): IdResponse
}
@Service
class PriceHistoryServiceImpl(
    private val repo: PriceHistoryRepository,
    private val mapper: PriceHistoryMapper
) : PriceHistoryService {
    override fun list(productId: String, storeId: String) =
        mapper.toResults(repo.findByProductIdAndStoreIdOrderByRecordedAtDesc(productId, storeId))

    override fun create(input: PriceHistoryInput): IdResponse {
        val e = mapper.fromInput(input)
        return IdResponse(repo.save(e).id!!)
    }
}

/* ======= PRODUCT RATING ======= */
interface ProductRatingService {
    fun upsert(input: ProductRatingInput): IdResponse
    fun update(id: String, update: ProductRatingUpdate): ProductRatingResult
}
@Service
class ProductRatingServiceImpl(
    private val repo: ProductRatingRepository,
    private val mapper: ProductRatingMapper
) : ProductRatingService {
    override fun upsert(input: ProductRatingInput): IdResponse {
        val existing = repo.findByUserIdAndProductId(input.userId!!, input.productId!!)
        return if (existing != null) {
            mapper.merge(existing, ProductRatingUpdate(input.qualityScore, input.notes))
            IdResponse(repo.save(existing).id!!)
        } else {
            IdResponse(repo.save(mapper.fromInput(input)).id!!)
        }
    }
    override fun update(id: String, update: ProductRatingUpdate): ProductRatingResult {
        val e = repo.findById(id).orElseThrow()
        mapper.merge(e, update)
        return mapper.toResult(repo.save(e))
    }
}
