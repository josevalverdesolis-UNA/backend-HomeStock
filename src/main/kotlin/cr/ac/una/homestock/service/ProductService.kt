package cr.ac.una.homestock.service

import cr.ac.una.homestock.domain.repository.ProductRepository
import cr.ac.una.homestock.dto.ProductDto
import cr.ac.una.homestock.mapper.ProductMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProductService(
    private val repository: ProductRepository,
    private val mapper: ProductMapper
) {
    fun getAll(): List<ProductDto> = mapper.toDtoList(repository.findAll())
    fun getById(id: Long): ProductDto? = repository.findById(id)?.let { mapper.toDto(it) }
    fun create(dto: ProductDto): ProductDto {
        val domain = mapper.toDomain(dto.copy(id = null))
        return mapper.toDto(repository.save(domain))
    }
    fun update(id: Long, dto: ProductDto): ProductDto? {
        val existing = repository.findById(id) ?: return null
        val updatedDomain = existing.copy(name = dto.name, quantity = dto.quantity)
        return mapper.toDto(repository.save(updatedDomain))
    }
    fun delete(id: Long): Boolean {
        val exists = repository.findById(id) != null
        if (exists) repository.deleteById(id)
        return exists
    }
}

