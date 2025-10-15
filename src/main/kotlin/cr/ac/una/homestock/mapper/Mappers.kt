package cr.una.homestock.mapper

import cr.una.homestock.domain.dto.*
import cr.una.homestock.domain.model.*
import org.mapstruct.*

@Mapper(componentModel = "spring")
interface ProductMapper {

    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "storeId", target = "store.id")
    fun toEntity(input: ProductInput): Product

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "storeId", target = "store.id")
    fun partialUpdate(input: ProductUpdate, @MappingTarget entity: Product)

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "store.id", target = "storeId")
    fun toResult(entity: Product): ProductResult

    companion object {
        fun toResultStatic(entity: Product) = ProductResult(
            id = entity.id!!,
            userId = entity.user.id!!,
            categoryId = entity.category?.id,
            storeId = entity.store?.id,
            name = entity.name,
            brand = entity.brand,
            quantity = entity.quantity,
            minStock = entity.minStock,
            acquisitionDate = entity.acquisitionDate,
            price = entity.price,
            imageUrl = entity.imageUrl
        )
    }
}

@Mapper(componentModel = "spring")
interface MovementMapper {

    @Mapping(source = "product.id", target = "productId")
    fun toResult(entity: Movement): MovementResult

    companion object {
        fun toResultStatic(entity: Movement) = MovementResult(
            id = entity.id!!,
            productId = entity.product.id!!,
            type = entity.type,
            quantity = entity.quantity,
            unitPrice = entity.unitPrice,
            occurredAt = entity.occurredAt,
            note = entity.note
        )
    }
}
