@file:Suppress("unused")
package cr.ac.una.homestock.web

import cr.ac.una.homestock.dto.*
import cr.ac.una.homestock.service.*
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

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
    @Operation(summary = "Listar categorías")
    @GetMapping
    fun list(): List<CategoryResult> = service.list()

    @Operation(summary = "Crear categoría")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody body: CategoryCreate): CategoryResult = service.create(body)

    @Operation(summary = "Actualizar categoría")
    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody body: CategoryUpdate): CategoryResult =
        service.update(id, body)

    @Operation(summary = "Eliminar categoría")
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
    @Operation(summary = "Listar tiendas")
    @GetMapping
    fun list(): List<StoreResult> = service.list()

    @Operation(summary = "Crear tienda")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody body: StoreCreate): StoreResult = service.create(body)

    @Operation(summary = "Actualizar tienda")
    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody body: StoreUpdate): StoreResult =
        service.update(id, body)

    @Operation(summary = "Eliminar tienda")
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
    @Operation(summary = "Listar productos de un usuario")
    @GetMapping("/users/{userId}/products")
    fun byUser(@PathVariable userId: Long): List<ProductResult> = service.byUser(userId)

    @Operation(summary = "Buscar/listar productos (paginado)")
    @GetMapping("/products")
    fun search(
        @RequestParam userId: Long,
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(defaultValue = "false") minStockOnly: Boolean,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PageResponse<ProductResult> = service.search(userId, q, categoryId, minStockOnly, page, size)

    @Operation(summary = "Detalle de producto por id")
    @GetMapping("/products/{id}")
    fun getById(@PathVariable id: Long): ProductResult = service.getById(id)

    @Operation(summary = "Buscar producto por código de barras")
    @GetMapping("/products/barcode/{code}")
    fun byBarcode(
        @RequestParam userId: Long,
        @PathVariable code: String
    ): ProductResult = service.findByBarcode(userId, code)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found")

    @Operation(summary = "Obtener producto de un usuario por id")
    @GetMapping("/users/{userId}/products/{id}")
    fun get(@PathVariable userId: Long, @PathVariable id: Long): ProductResult = service.get(userId, id)

    @Operation(summary = "Crear producto")
    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody body: ProductCreate): ProductResult = service.create(body)

    @Operation(summary = "Actualizar producto")
    @PatchMapping("/products/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody body: ProductUpdate): ProductResult =
        service.update(id, body)

    @Operation(summary = "Eliminar producto")
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
    @Operation(summary = "Registrar movimiento de inventario")
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
    @Operation(summary = "Listar ítems de compra pendientes de un usuario")
    @GetMapping("/users/{userId}/shopping-items")
    fun listPending(@PathVariable userId: Long): List<ShoppingItemResult> = service.listPending(userId)

    @Operation(summary = "Crear ítem de lista de compras")
    @PostMapping("/shopping-items")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody body: ShoppingItemCreate): ShoppingItemResult = service.create(body)

    @Operation(summary = "Actualizar ítem de lista de compras")
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
    @Operation(summary = "Listar alertas activas de un usuario")
    @GetMapping("/users/{userId}/alerts/active")
    fun listActive(@PathVariable userId: Long): List<AlertResult> = service.listActive(userId)

    @Operation(summary = "Crear alerta")
    @PostMapping("/alerts")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody body: AlertCreate): AlertResult = service.create(body)

    @Operation(summary = "Actualizar alerta")
    @PatchMapping("/alerts/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody body: AlertUpdate): AlertResult = service.update(id, body)

    @Operation(summary = "Cerrar alerta")
    @PatchMapping("/alerts/{id}/close")
    fun close(@PathVariable id: Long): AlertResult = service.close(id)
}

@Validated
@RestController
@RequestMapping("/api/v1/price-history")
class PriceHistoryController(
    private val service: PriceHistoryService
) {
    @Operation(summary = "Agregar entrada al histórico de precios")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun add(@Valid @RequestBody body: PriceHistoryCreate): PriceHistoryResult = service.add(body)

    @Operation(summary = "Obtener último precio de un producto")
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
    @Operation(summary = "Crear o actualizar valoración de producto")
    @PutMapping
    fun upsert(@Valid @RequestBody body: ProductRatingCreate): ProductRatingResult = service.upsert(body)
}

// Comentario de cambios: agregado @Operation(summary = ...) a todos los endpoints
