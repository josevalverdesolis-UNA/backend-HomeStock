package cr.ac.una.homestock.web

import cr.ac.una.homestock.dto.CategoryDto
import cr.ac.una.homestock.dto.CreateCategoryDto
import cr.ac.una.homestock.dto.UpdateCategoryDto
import cr.ac.una.homestock.service.CategoryService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val service: CategoryService
) {
    @GetMapping
    fun list(): List<CategoryDto> = service.list()

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): CategoryDto = service.get(id)

    @PostMapping
    fun create(@Valid @RequestBody body: CreateCategoryDto): ResponseEntity<CategoryDto> {
        val dto = service.create(body)
        return ResponseEntity.created(URI.create("/api/categories/${dto.id}")).body(dto)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody body: UpdateCategoryDto): CategoryDto =
        service.update(id, body)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}

