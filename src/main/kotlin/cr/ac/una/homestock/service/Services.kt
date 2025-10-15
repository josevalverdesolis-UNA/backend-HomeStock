package cr.una.homestock.service

import cr.una.homestock.domain.dto.*
import cr.una.homestock.domain.model.*
import cr.una.homestock.mapper.MovementMapper
import cr.una.homestock.mapper.ProductMapper
import cr.una.homestock.repository.*
import cr.una.homestock.web.error.BusinessException
import cr.una.homestock.web.error.NotFoundException
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneOffset

/* =========================
   PRODUCT SERVICE
   ========================= */

@Service
class ProductService(
    private val productRepo: ProductRepository,
    private val userRepo: UserRepository,
    private val categoryRepo: CategoryRepository,
    private val storeRepo: StoreRepository,
    private val shoppingRepo: ShoppingItemRepository,
    private val mapper: ProductMapper
) {

    @Transactional
    fun create(input: ProductInput): ProductResult {
        // Solo validaciones de existencia “lógica” (opcionales si confías en FK)
        userRepo.findById(input.userId).orElseThrow { NotFoundException("Usuario no existe") }
        input.categoryId?.let {
            if (!categoryRepo.existsById(it)) throw NotFoundException("Categoría no existe")
        }
        input.storeId?.let {
            if (!storeRepo.existsById(it)) throw NotFoundException("Tienda no existe")
        }

        val entity = mapper.toEntity(input)
        val saved = productRepo.save(entity)
        ensureAutoShoppingItem(saved)
        return mapper.toResult(saved)
    }

    @Transactional
    fun update(id: String, input: ProductUpdate): ProductResult {
        val entity = productRepo.findById(id).orElseThrow { NotFoundException("Producto no existe") }
        input.categoryId?.let {
            if (!categoryRepo.existsById(it)) throw NotFoundException("Categoría no existe")
        }
        input.storeId?.let {
            if (!storeRepo.existsById(it)) throw NotFoundException("Tienda no existe")
        }
        mapper.partialUpdate(input, entity)
        val saved = productRepo.save(entity)
        ensureAutoShoppingItem(saved)
        return mapper.toResult(saved)
    }

    fun listByUser(userId: String, pageable: Pageable): Page<ProductResult> =
        productRepo.findAllByUserId(userId, pageable).map(mapper::toResult)

    fun listByUserAndCategory(userId: String, categoryId: String, pageable: Pageable): Page<ProductResult> =
        productRepo.findByUserIdAndCategoryId(userId, categoryId, pageable).map(mapper::toResult)

    private fun ensureAutoShoppingItem(product: Product) {
        val qty = product.quantity
        val min = product.minStock
        val needsRestock = qty <= min

        val active = shoppingRepo.findActiveByProductId(product.id!!)
        if (needsRestock) {
            if (active == null) {
                shoppingRepo.save(
                    ShoppingItem(
                        product = product,
                        quantity = (min - qty).coerceAtLeast(1),
                        isPurchased = false,
                        source = ShoppingSource.AUTO_RULE,
                        createdAt = OffsetDateTime.now(ZoneOffset.UTC)
                    )
                )
            } else {
                active.quantity = (min - qty).coerceAtLeast(1)
                shoppingRepo.save(active)
            }
        } else if (active != null && active.source == ShoppingSource.AUTO_RULE) {
            shoppingRepo.delete(active)
        }
    }
}

/* =========================
   MOVEMENT SERVICE
   ========================= */

@Service
class MovementService(
    private val movementRepo: MovementRepository,
    private val productRepo: ProductRepository,
    private val mapper: MovementMapper
) {

    @Transactional
    fun create(input: MovementInput): MovementResult {
        val product = productRepo.findById(input.productId)
            .orElseThrow { NotFoundException("Producto no existe") }

        // Reglas de negocio: unitPrice y signo
        if (input.type == MovementType.PURCHASE && input.unitPrice == null) {
            throw BusinessException("unitPrice es requerido para compras (PURCHASE)")
        }

        val normalizedQty = when (input.type) {
            MovementType.PURCHASE -> {
                if (input.quantity <= 0) throw BusinessException("quantity debe ser > 0 en PURCHASE")
                input.quantity
            }
            MovementType.CONSUMPTION -> if (input.quantity >= 0) -input.quantity else input.quantity
        }

        val entity = Movement(
            product = product,
            type = input.type,
            quantity = normalizedQty,
            unitPrice = input.unitPrice,
            occurredAt = input.occurredAt ?: OffsetDateTime.now(ZoneOffset.UTC),
            note = input.note
        )
        val saved = movementRepo.save(entity)

        // Stock del producto
        product.quantity = product.quantity + normalizedQty
        productRepo.save(product)

        return mapper.toResult(saved)
    }

    @Transactional
    fun update(id: String, input: MovementUpdate): MovementResult {
        val mov = movementRepo.findById(id).orElseThrow { NotFoundException("Movimiento no existe") }

        val newType = input.type ?: mov.type
        var newQty = input.quantity ?: mov.quantity
        val newUnitPrice = input.unitPrice ?: mov.unitPrice

        if (newType == MovementType.PURCHASE && newUnitPrice == null) {
            throw BusinessException("unitPrice es requerido para compras (PURCHASE)")
        }

        newQty = when (newType) {
            MovementType.PURCHASE -> {
                if (newQty <= 0) throw BusinessException("quantity debe ser > 0 en PURCHASE")
                newQty
            }
            MovementType.CONSUMPTION -> if (newQty >= 0) -newQty else newQty
        }

        val product = mov.product
        val current = product.quantity
        val reverted = current - mov.quantity
        product.quantity = reverted + newQty

        mov.type = newType
        mov.quantity = newQty
        mov.unitPrice = newUnitPrice
        mov.occurredAt = input.occurredAt ?: mov.occurredAt
        mov.note = input.note ?: mov.note

        productRepo.save(product)
        return mapper.toResult(movementRepo.save(mov))
    }
}
