package cr.una.homestock.domain.mapper

import cr.una.homestock.domain.model.*
import cr.una.homestock.web.dto.*
import org.mapstruct.*

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
interface UserMapper {
    fun toResult(entity: User): UserResult
    fun toResults(list: List<User>): List<UserResult>
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    fun fromInput(input: UserInput): User
    fun merge(@MappingTarget entity: User, update: UserUpdate)
}

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface CategoryMapper {
    fun toResult(entity: Category): CategoryResult
    fun toResults(list: List<Category>): List<CategoryResult>
    @Mapping(target = "id", ignore = true)
    fun fromInput(input: CategoryInput): Category
    fun merge(@MappingTarget entity: Category, update: CategoryUpdate)
}

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface StoreMapper {
    fun toResult(entity: Store): StoreResult
    fun toResults(list: List<Store>): List<StoreResult>
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    fun fromInput(input: StoreInput): Store
    fun merge(@MappingTarget entity: Store, update: StoreUpdate)
}

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface ProductMapper {
    fun toResult(entity: Product): ProductResult
    fun toResults(list: List<Product>): List<ProductResult>

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.OffsetDateTime.now())")
    fun fromInput(input: ProductInput): Product

    @Mapping(target = "updatedAt", expression = "java(java.time.OffsetDateTime.now())")
    fun merge(@MappingTarget entity: Product, update: ProductUpdate)
}

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface MovementMapper {
    @Mapping(target = "type", expression = "java(cr.una.homestock.domain.model.MovementType.valueOf(input.getType().name()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "occurredAt", expression = "java(input.getOccurredAt() != null ? input.getOccurredAt() : java.time.OffsetDateTime.now())")
    fun fromInput(input: MovementInput): Movement

    @Mapping(target = "type", expression = "java(cr.una.homestock.web.dto.MovementTypeDto.valueOf(entity.getType().name()))")
    fun toResult(entity: Movement): MovementResult
    fun toResults(list: List<Movement>): List<MovementResult>
}

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface ShoppingItemMapper {
    @Mapping(target = "source", expression = "java(cr.una.homestock.domain.model.ShoppingSource.valueOf((input.getSource()!=null?input.getSource():cr.una.homestock.web.dto.ShoppingSourceDto.MANUAL).name()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "isPurchased", constant = "false")
    fun fromInput(input: ShoppingItemInput): ShoppingItem

    fun toResult(entity: ShoppingItem): ShoppingItemResult
    fun toResults(list: List<ShoppingItem>): List<ShoppingItemResult>
    fun merge(@MappingTarget entity: ShoppingItem, update: ShoppingItemUpdate)
}

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface AlertMapper {
    @Mapping(target = "type", expression = "java(cr.una.homestock.domain.model.AlertType.valueOf(input.getType().name()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "triggerAt", expression = "java(input.getTriggerAt()!=null?input.getTriggerAt():java.time.OffsetDateTime.now())")
    @Mapping(target = "isActive", constant = "true")
    fun fromInput(input: AlertInput): Alert

    @Mapping(target = "type", expression = "java(cr.una.homestock.web.dto.AlertTypeDto.valueOf(entity.getType().name()))")
    fun toResult(entity: Alert): AlertResult
    fun toResults(list: List<Alert>): List<AlertResult>

    fun merge(@MappingTarget entity: Alert, update: AlertUpdate)
}

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface PriceHistoryMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recordedAt", expression = "java(input.getRecordedAt()!=null?input.getRecordedAt():java.time.OffsetDateTime.now())")
    fun fromInput(input: PriceHistoryInput): PriceHistory

    fun toResult(entity: PriceHistory): PriceHistoryResult
    fun toResults(list: List<PriceHistory>): List<PriceHistoryResult>
}

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface ProductRatingMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    fun fromInput(input: ProductRatingInput): ProductRating

    fun toResult(entity: ProductRating): ProductRatingResult
    fun toResults(list: List<ProductRating>): List<ProductRatingResult>
    fun merge(@MappingTarget entity: ProductRating, update: ProductRatingUpdate)
}
