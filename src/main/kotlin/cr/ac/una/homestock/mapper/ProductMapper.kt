package cr.ac.una.homestock.mapper

import cr.ac.una.homestock.data.entity.ProductEntity
import cr.ac.una.homestock.domain.model.Product
import cr.ac.una.homestock.dto.ProductDto
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface ProductMapper {
    fun toDomain(entity: ProductEntity): Product
    fun toEntity(domain: Product): ProductEntity
    fun toDto(domain: Product): ProductDto
    fun toDomain(dto: ProductDto): Product

    fun toDomainList(entities: List<ProductEntity>): List<Product>
    fun toDtoList(domains: List<Product>): List<ProductDto>
}

