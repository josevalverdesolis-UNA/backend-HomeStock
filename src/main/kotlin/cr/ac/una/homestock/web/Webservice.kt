package cr.ac.una.homestock.web

import cr.ac.una.homestock.dto.*
import cr.ac.una.homestock.service.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/**
 * Controladores REST (v1) alineados con DTOs/Servicios.
 * Rutas base: /api/v1
 */

@Validated
@RestController
@RequestMapping("/api/v1/categories")
class CategoryController(
    private val service: CategoryService
) {
    @GetMapping
    fun list(): List<CategoryResult> = service.list()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody body: CategoryCreate): CategoryResult = service.create(body)

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody body: CategoryUpdate): CategoryResult =
        service.update(id, body)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = service.delete(id)
}

@Validated
@RestController
@RequestMapping("/api/v1/stores")
class StoreController(
    private val service: StoreService
) {
    @GetMapping
    fun list(): List<StoreResult> = service.list()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody body: StoreCreate): StoreResult = service.create(body)

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody body: StoreUpdate): StoreResult =
        service.update(id, body)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = service.delete(id)
}

@Validated
@RestController
@RequestMapping("/api/v1")
class ProductController(
    private val service: ProductService
) {
    @GetMapping("/users/{userId}/products")
    fun byUser(@PathVariable userId: Long): List<ProductResult> = service.byUser(userId)

    @GetMapping("/users/{userId}/products/{id}")
    fun get(@PathVariable userId: Long, @PathVariable id: Long): ProductResult = service.get(userId, id)

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody body: ProductCreate): ProductResult = service.create(body)

    @PatchMapping("/products/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody body: ProductUpdate): ProductResult =
        service.update(id, body)

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = service.delete(id)
}

@Validated
@RestController
@RequestMapping("/api/v1/movements")
class MovementController(
    private val service: MovementService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody body: MovementCreate): MovementResult = service.create(body)
}

@Validated
@RestController
@RequestMapping("/api/v1")
class ShoppingItemController(
    private val service: ShoppingItemService
) {
    @GetMapping("/users/{userId}/shopping-items")
    fun listPending(@PathVariable userId: Long): List<ShoppingItemResult> = service.listPending(userId)

    @PostMapping("/shopping-items")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody body: ShoppingItemCreate): ShoppingItemResult = service.create(body)

    @PatchMapping("/shopping-items/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody body: ShoppingItemUpdate): ShoppingItemResult =
        service.update(id, body)
}

@Validated
@RestController
@RequestMapping("/api/v1")
class AlertController(
    private val service: AlertService
) {
    @GetMapping("/users/{userId}/alerts/active")
    fun listActive(@PathVariable userId: Long): List<AlertResult> = service.listActive(userId)

    @PostMapping("/alerts")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody body: AlertCreate): AlertResult = service.create(body)

    @PatchMapping("/alerts/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody body: AlertUpdate): AlertResult = service.update(id, body)

    @PatchMapping("/alerts/{id}/close")
    fun close(@PathVariable id: Long): AlertResult = service.close(id)
}

@Validated
@RestController
@RequestMapping("/api/v1/price-history")
class PriceHistoryController(
    private val service: PriceHistoryService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun add(@Valid @RequestBody body: PriceHistoryCreate): PriceHistoryResult = service.add(body)

    @GetMapping("/products/{productId}/last")
    fun lastForProduct(@PathVariable productId: Long): PriceHistoryResult? = service.lastForProduct(productId)
}

@Validated
@RestController
@RequestMapping("/api/v1/ratings")
class ProductRatingController(
    private val service: ProductRatingService
) {
    /**
     * Upsert idempotente por (userId, productId).
     */
    @PutMapping
    fun upsert(@Valid @RequestBody body: ProductRatingCreate): ProductRatingResult = service.upsert(body)
}

// Comentario de cambios: creado/actualizado archivo -> Webservice.kt
