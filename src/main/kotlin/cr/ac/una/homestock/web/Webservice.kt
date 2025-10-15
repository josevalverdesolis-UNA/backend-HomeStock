package cr.una.homestock.web

import cr.una.homestock.domain.dto.*
import cr.una.homestock.service.MovementService
import cr.una.homestock.service.ProductService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

/* =========================
   PRODUCT CONTROLLER
   ========================= */

@RestController
@RequestMapping("/v1/products")
class ProductController(
    private val productService: ProductService
) {

    @GetMapping
    fun list(
        @RequestParam userId: String,
        @RequestParam(required = false) categoryId: String?,
        @PageableDefault(page = 0, size = 20, sort = ["name"]) pageable: Pageable
    ): Page<ProductResult> =
        if (categoryId.isNullOrBlank())
            productService.listByUser(userId, pageable)
        else
            productService.listByUserAndCategory(userId, categoryId, pageable)

    @PostMapping
    fun create(@Valid @RequestBody input: ProductInput): ProductResult =
        productService.create(input)

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: String,
        @Valid @RequestBody input: ProductUpdate
    ): ProductResult = productService.update(id, input)
}

/* =========================
   MOVEMENT CONTROLLER
   ========================= */

@RestController
@RequestMapping("/v1/movements")
class MovementController(
    private val movementService: MovementService
) {
    @PostMapping
    fun create(@Valid @RequestBody input: MovementInput): MovementResult =
        movementService.create(input)

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: String,
        @Valid @RequestBody input: MovementUpdate
    ): MovementResult = movementService.update(id, input)
}
