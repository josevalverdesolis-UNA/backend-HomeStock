package cr.ac.una.homestock.web



import cr.ac.una.homestock.dto.*
import cr.ac.una.homestock.service.ProductService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/")
class HomeController {
    @GetMapping
    fun home(): String = "Servidor activo"
}

@RestController
@RequestMapping("/api/health")
class HealthController {
    @GetMapping fun ok() = mapOf("status" to "UP")
}

@RestController
@RequestMapping("/api/products")
class ProductController(private val service: ProductService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) userId: String?,
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) categoryId: UUID?
    ): List<ProductDto> = service.list(userId, q, categoryId)

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): ProductDto = service.get(id)

    @PostMapping
    fun create(@Valid @RequestBody body: CreateProductDto): ResponseEntity<ProductDto> {
        val dto = service.create(body)
        return ResponseEntity.created(URI.create("/api/products/${dto.id}")).body(dto)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody body: UpdateProductDto): ProductDto =
        service.update(id, body)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}
