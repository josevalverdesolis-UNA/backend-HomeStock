package cr.ac.una.homestock.data.repository

import cr.ac.una.homestock.domain.model.Product
import cr.ac.una.homestock.domain.repository.ProductRepository
import cr.ac.una.homestock.mapper.ProductMapper
import org.springframework.stereotype.Component

@Component
class ProductRepositoryAdapter(
    private val jpaRepository: ProductJpaRepository,
    private val mapper: ProductMapper
): ProductRepository {
    override fun findAll(): List<Product> = mapper.toDomainList(jpaRepository.findAll())
    override fun findById(id: Long): Product? = jpaRepository.findById(id).map { mapper.toDomain(it) }.orElse(null)
    override fun save(product: Product): Product {
        val entity = mapper.toEntity(product)
        val saved = jpaRepository.save(entity)
        return mapper.toDomain(saved)
    }
    override fun deleteById(id: Long) = jpaRepository.deleteById(id)
}

