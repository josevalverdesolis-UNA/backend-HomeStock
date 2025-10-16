package cr.ac.una.homestock.web

import cr.ac.una.homestock.dto.*
import cr.ac.una.homestock.service.MovementService
import cr.ac.una.homestock.service.ProductService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.util.*

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
            productService.listByUser(UUID.fromString(userId), pageable)
        else
            productService.listByUserAndCategory(UUID.fromString(userId), UUID.fromString(categoryId), pageable)

    @PostMapping
    fun create(@Valid @RequestBody input: ProductInput): ProductResult =
        productService.create(input)

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: String,
        @Valid @RequestBody input: ProductUpdateInput
    ): ProductResult = productService.update(UUID.fromString(id), input)
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
}
