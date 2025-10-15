package cr.ac.una.homestock.mapper

import cr.ac.una.homestock.data.*
import cr.ac.una.homestock.dto.*
import org.mapstruct.*

/* Reglas globales */
private val IGNORE_NULL = NullValuePropertyMappingStrategy.IGNORE

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface UserMapper {
    fun toResult(e: UserEntity): UserResult

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE_NULL)
    fun merge(@MappingTarget e: UserEntity, i: UserInput)
}

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface CategoryMapper {
    fun toResult(e: CategoryEntity): CategoryResult

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE_NULL)
    fun merge(@MappingTarget e: CategoryEntity, i: CategoryInput)
}

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface StoreMapper {
    fun toResult(e: StoreEntity): StoreResult

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE_NULL)
    fun merge(@MappingTarget e: StoreEntity, i: StoreInput)
}

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = [CategoryMapper::class])
interface ProductMapper {
    @Mappings(
        Mapping(target = "category", source = "category")
    )
    fun toResult(e: ProductEntity): ProductResult

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE_NULL)
    fun merge(@MappingTarget e: ProductEntity, i: ProductInput)
}

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface MovementMapper {
    @Mappings(
        Mapping(target = "productId", source = "product.id"),
        Mapping(target = "userId", source = "user.id"),
        Mapping(target = "storeId", source = "store.id")
    )
    fun toResult(e: MovementEntity): MovementResult

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE_NULL)
    fun merge(@MappingTarget e: MovementEntity, i: MovementInput)
}

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface ShoppingItemMapper {
    @Mappings(
        Mapping(target = "productId", source = "product.id"),
        Mapping(target = "userId", source = "user.id"),
        Mapping(target = "targetStoreId", source = "targetStore.id")
    )
    fun toResult(e: ShoppingItemEntity): ShoppingItemResult

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE_NULL)
    fun merge(@MappingTarget e: ShoppingItemEntity, i: ShoppingItemInput)
}

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface AlertMapper {
    @Mappings(
        Mapping(target = "productId", source = "product.id"),
        Mapping(target = "userId", source = "user.id"),
    )
    fun toResult(e: AlertEntity): AlertResult

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE_NULL)
    fun merge(@MappingTarget e: AlertEntity, i: AlertInput)
}

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface PriceHistoryMapper {
    @Mappings(
        Mapping(target = "productId", source = "product.id"),
        Mapping(target = "storeId", source = "store.id"),
    )
    fun toResult(e: PriceHistoryEntity): PriceHistoryResult

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE_NULL)
    fun merge(@MappingTarget e: PriceHistoryEntity, i: PriceHistoryInput)
}

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface ProductRatingMapper {
    @Mappings(
        Mapping(target = "productId", source = "product.id"),
        Mapping(target = "userId", source = "user.id")
    )
    fun toResult(e: ProductRatingEntity): ProductRatingResult

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE_NULL)
    fun merge(@MappingTarget e: ProductRatingEntity, i: ProductRatingInput)
}
