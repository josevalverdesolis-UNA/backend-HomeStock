package cr.una.homestock.web.controller

import cr.una.homestock.domain.service.*
import cr.una.homestock.web.dto.*
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1")
class ApiController(
    private val userSvc: UserService,
    private val categorySvc: CategoryService,
    private val storeSvc: StoreService,
    private val productSvc: ProductService,
    private val movementSvc: MovementService,
    private val shoppingSvc: ShoppingItemService,
    private val alertSvc: AlertService,
    private val priceSvc: PriceHistoryService,
    private val ratingSvc: ProductRatingService
) {
    /* ===== USERS ===== */
    @GetMapping("/users")
    fun users() = ResponseEntity.ok(ApiResponse(userSvc.list()))
    @GetMapping("/users/{id}")
    fun user(@PathVariable id: String) = ResponseEntity.ok(ApiResponse(userSvc.get(id)))
    @PostMapping("/users")
    fun createUser(@Valid @RequestBody body: UserInput) =
        ResponseEntity.ok(ApiResponse(userSvc.create(body)))
    @PutMapping("/users/{id}")
    fun updateUser(@PathVariable id: String, @Valid @RequestBody body: UserUpdate) =
        ResponseEntity.ok(ApiResponse(userSvc.update(id, body)))
    @DeleteMapping("/users/{id}")
    fun deleteUser(@PathVariable id: String) =
        ResponseEntity.ok().build<Void>()

    /* ===== CATEGORIES ===== */
    @GetMapping("/categories")
    fun categories() = ResponseEntity.ok(ApiResponse(categorySvc.list()))
    @GetMapping("/categories/{id}")
    fun category(@PathVariable id: String) = ResponseEntity.ok(ApiResponse(categorySvc.get(id)))
    @PostMapping("/categories")
    fun createCategory(@Valid @RequestBody body: CategoryInput) =
        ResponseEntity.ok(ApiResponse(categorySvc.create(body)))
    @PutMapping("/categories/{id}")
    fun updateCategory(@PathVariable id: String, @Valid @RequestBody body: CategoryUpdate) =
        ResponseEntity.ok(ApiResponse(categorySvc.update(id, body)))
    @DeleteMapping("/categories/{id}")
    fun deleteCategory(@PathVariable id: String) =
        ResponseEntity.ok().build<Void>()

    /* ===== STORES ===== */
    @GetMapping("/stores")
    fun stores() = ResponseEntity.ok(ApiResponse(storeSvc.list()))
    @GetMapping("/stores/{id}")
    fun store(@PathVariable id: String) = ResponseEntity.ok(ApiResponse(storeSvc.get(id)))
    @PostMapping("/stores")
    fun createStore(@Valid @RequestBody body: StoreInput) =
        ResponseEntity.ok(ApiResponse(storeSvc.create(body)))
    @PutMapping("/stores/{id}")
    fun updateStore(@PathVariable id: String, @Valid @RequestBody body: StoreUpdate) =
        ResponseEntity.ok(ApiResponse(storeSvc.update(id, body)))
    @DeleteMapping("/stores/{id}")
    fun deleteStore(@PathVariable id: String) =
        ResponseEntity.ok().build<Void>()

    /* ===== PRODUCTS ===== */
    @GetMapping("/products")
    fun productsByUser(@RequestParam userId: String) =
        ResponseEntity.ok(ApiResponse(productSvc.listByUser(userId)))
    @GetMapping("/products/{id}")
    fun product(@PathVariable id: String) =
        ResponseEntity.ok(ApiResponse(productSvc.get(id)))
    @PostMapping("/products")
    fun createProduct(@Valid @RequestBody body: ProductInput) =
        ResponseEntity.ok(ApiResponse(productSvc.create(body)))
    @PutMapping("/products/{id}")
    fun updateProduct(@PathVariable id: String, @Valid @RequestBody body: ProductUpdate) =
        ResponseEntity.ok(ApiResponse(productSvc.update(id, body)))
    @DeleteMapping("/products/{id}")
    fun deleteProduct(@PathVariable id: String) =
        ResponseEntity.ok().build<Void>()

    /* ===== MOVEMENTS ===== */
    @GetMapping("/movements")
    fun movementsByProduct(@RequestParam productId: String) =
        ResponseEntity.ok(ApiResponse(movementSvc.listByProduct(productId)))
    @GetMapping("/movements/{id}")
    fun movement(@PathVariable id: String) =
        ResponseEntity.ok(ApiResponse(movementSvc.get(id)))
    @PostMapping("/movements")
    fun createMovement(@Valid @RequestBody body: MovementInput) =
        ResponseEntity.ok(ApiResponse(movementSvc.create(body)))

    /* ===== SHOPPING ITEMS ===== */
    @GetMapping("/shopping-items")
    fun shoppingItems(@RequestParam userId: String) =
        ResponseEntity.ok(ApiResponse(shoppingSvc.listActiveByUser(userId)))
    @PostMapping("/shopping-items")
    fun createShoppingItem(@Valid @RequestBody body: ShoppingItemInput) =
        ResponseEntity.ok(ApiResponse(shoppingSvc.create(body)))
    @PutMapping("/shopping-items/{id}")
    fun updateShoppingItem(@PathVariable id: String, @Valid @RequestBody body: ShoppingItemUpdate) =
        ResponseEntity.ok(ApiResponse(shoppingSvc.update(id, body)))
    @DeleteMapping("/shopping-items/{id}")
    fun deleteShoppingItem(@PathVariable id: String) =
        ResponseEntity.ok().build<Void>()

    /* ===== ALERTS ===== */
    @GetMapping("/alerts")
    fun alerts(@RequestParam userId: String) =
        ResponseEntity.ok(ApiResponse(alertSvc.listActiveByUser(userId)))
    @PostMapping("/alerts")
    fun createAlert(@Valid @RequestBody body: AlertInput) =
        ResponseEntity.ok(ApiResponse(alertSvc.create(body)))
    @PutMapping("/alerts/{id}")
    fun updateAlert(@PathVariable id: String, @Valid @RequestBody body: AlertUpdate) =
        ResponseEntity.ok(ApiResponse(alertSvc.update(id, body)))

    /* ===== PRICE HISTORY ===== */
    @GetMapping("/price-history")
    fun priceHistory(@RequestParam productId: String, @RequestParam storeId: String) =
        ResponseEntity.ok(ApiResponse(priceSvc.list(productId, storeId)))
    @PostMapping("/price-history")
    fun createPriceHistory(@Valid @RequestBody body: PriceHistoryInput) =
        ResponseEntity.ok(ApiResponse(priceSvc.create(body)))

    /* ===== PRODUCT RATINGS ===== */
    @PostMapping("/product-ratings")
    fun upsertRating(@Valid @RequestBody body: ProductRatingInput) =
        ResponseEntity.ok(ApiResponse(ratingSvc.upsert(body)))
    @PutMapping("/product-ratings/{id}")
    fun updateRating(@PathVariable id: String, @Valid @RequestBody body: ProductRatingUpdate) =
        ResponseEntity.ok(ApiResponse(ratingSvc.update(id, body)))
}
