package cr.ac.una.homestock.mapper

import cr.ac.una.homestock.domain.model.*
import cr.ac.una.homestock.dto.*
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface ProductMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "category.id", target = "categoryId")
    fun toResult(entity: Product): ProductResult
}

@Mapper(componentModel = "spring")
interface MovementMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "store.id", target = "storeId")
    fun toResult(entity: Movement): MovementResult
}

@Mapper(componentModel = "spring")
interface CategoryMapper {
    @Mapping(source = "user.id", target = "userId")
    fun toResult(entity: Category): CategoryResult
}

@Mapper(componentModel = "spring")
interface StoreMapper {
    @Mapping(source = "user.id", target = "userId")
    fun toResult(entity: Store): StoreResult
}

@Mapper(componentModel = "spring")
interface ShoppingItemMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "targetStore.id", target = "targetStoreId")
    fun toResult(entity: ShoppingItem): ShoppingItemResult
}

@Mapper(componentModel = "spring")
interface AlertMapper {
    @Mapping(source = "user.id", target = "userId")
    fun toResult(entity: Alert): AlertResult
}

@Mapper(componentModel = "spring")
interface PriceHistoryMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "store.id", target = "storeId")
    fun toResult(entity: PriceHistory): PriceHistoryResult
}

@Mapper(componentModel = "spring")
interface ProductRatingMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "product.id", target = "productId")
    fun toResult(entity: ProductRating): ProductRatingResult
}
