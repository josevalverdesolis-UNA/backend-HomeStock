package cr.ac.una.homestock.web

import cr.ac.una.homestock.dto.ProductDto
import cr.ac.una.homestock.service.ProductService
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.net.URI // nuevo import para Location

@RestController
@RequestMapping("/api/products")
class ProductController(private val service: ProductService) {

    @GetMapping
    fun list(): List<ProductDto> = service.getAll()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<ProductDto> =
        service.getById(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @PostMapping
    fun create(@Validated @RequestBody dto: ProductDto): ResponseEntity<ProductDto> {
        val created = service.create(dto)
        // 201 Created con Location
        val location = URI.create("/api/products/${created.id}")
        return ResponseEntity.created(location).body(created)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Validated @RequestBody dto: ProductDto): ResponseEntity<ProductDto> {
        val updated = service.update(id, dto) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        val deleted = service.delete(id)
        return if (deleted) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
    }
}

