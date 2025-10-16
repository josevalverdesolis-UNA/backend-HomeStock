package cr.ac.una.homestock.mapper

import cr.ac.una.homestock.domain.entity.*
import cr.ac.una.homestock.dto.*
import org.mapstruct.*

/**
 * Mappers MapStruct alineados con DTOs y Entidades.
 * - Estrategia para updates parciales: IGNORE en nulls
 * - Conversión de IDs <-> entidades ligeras via helpers fromId()/toId()
 * - componentsModel = "spring" para inyección
 */

// Configuración común: silenciar propiedades destino no mapeadas (id/createdAt/updatedAt, relaciones gestionadas en servicio)
@MapperConfig(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
interface CommonMapperConfig

// ------------------------------
// Helpers comunes ID <-> Entidad
// ------------------------------

@Mapper(componentModel = "spring", config = CommonMapperConfig::class)
abstract class IdRefMapper {
    @Named("userFromId")
    open fun userFromId(id: Long?): User? = id?.let { User(id = it) }

    @Named("categoryFromId")
    open fun categoryFromId(id: Long?): Category? = id?.let { Category(id = it) }

    @Named("storeFromId")
    open fun storeFromId(id: Long?): Store? = id?.let { Store(id = it) }

    @Named("productFromId")
    open fun productFromId(id: Long?): Product? = id?.let { Product(id = it) }

    @Named("idFromUser")
    open fun idFromUser(user: User?): Long? = user?.id

    @Named("idFromCategory")
    open fun idFromCategory(category: Category?): Long? = category?.id

    @Named("idFromStore")
    open fun idFromStore(store: Store?): Long? = store?.id

    @Named("idFromProduct")
    open fun idFromProduct(product: Product?): Long? = product?.id
}

// ------------------------------
// Enum mapper DTO <-> Entity
// ------------------------------

@Mapper(componentModel = "spring", config = CommonMapperConfig::class)
interface EnumMapper {
    // MovementType
    fun toEntityType(src: cr.ac.una.homestock.dto.MovementType): cr.ac.una.homestock.domain.entity.MovementType =
        cr.ac.una.homestock.domain.entity.MovementType.valueOf(src.name)
    fun toDtoType(src: cr.ac.una.homestock.domain.entity.MovementType): cr.ac.una.homestock.dto.MovementType =
        cr.ac.una.homestock.dto.MovementType.valueOf(src.name)

    // ShoppingSource
    fun toEntitySource(src: cr.ac.una.homestock.dto.ShoppingSource): cr.ac.una.homestock.domain.entity.ShoppingSource =
        cr.ac.una.homestock.domain.entity.ShoppingSource.valueOf(src.name)
    fun toDtoSource(src: cr.ac.una.homestock.domain.entity.ShoppingSource): cr.ac.una.homestock.dto.ShoppingSource =
        cr.ac.una.homestock.dto.ShoppingSource.valueOf(src.name)

    // AlertType
    fun toEntityAlert(src: cr.ac.una.homestock.dto.AlertType): cr.ac.una.homestock.domain.entity.AlertType =
        cr.ac.una.homestock.domain.entity.AlertType.valueOf(src.name)
    fun toDtoAlert(src: cr.ac.una.homestock.domain.entity.AlertType): cr.ac.una.homestock.dto.AlertType =
        cr.ac.una.homestock.dto.AlertType.valueOf(src.name)
}

// ------------------------------
// Category
// ------------------------------

@Mapper(componentModel = "spring", config = CommonMapperConfig::class)
interface CategoryMapper {
    fun toEntity(input: CategoryCreate): Category

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    fun update(input: CategoryUpdate, @MappingTarget entity: Category)

    fun toResult(entity: Category): CategoryResult
}

// ------------------------------
// Store
// ------------------------------

@Mapper(componentModel = "spring", config = CommonMapperConfig::class)
interface StoreMapper {
    fun toEntity(input: StoreCreate): Store

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    fun update(input: StoreUpdate, @MappingTarget entity: Store)

    fun toResult(entity: Store): StoreResult
}

// ------------------------------
// Product
// ------------------------------

@Mapper(componentModel = "spring", uses = [IdRefMapper::class], config = CommonMapperConfig::class)
interface ProductMapper {

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "user", source = "userId", qualifiedByName = ["userFromId"]),
        Mapping(target = "category", source = "categoryId", qualifiedByName = ["categoryFromId"]),
        Mapping(target = "purchaseLocation", source = "purchaseLocationId", qualifiedByName = ["storeFromId"])
    )
    fun fromCreate(input: ProductCreate): Product

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings(
        Mapping(target = "user", ignore = true), // user no se actualiza por PATCH
        Mapping(target = "category", source = "categoryId", qualifiedByName = ["categoryFromId"]),
        Mapping(target = "purchaseLocation", source = "purchaseLocationId", qualifiedByName = ["storeFromId"])
    )
    fun update(input: ProductUpdate, @MappingTarget entity: Product)

    @Mappings(
        Mapping(target = "userId", source = "user", qualifiedByName = ["idFromUser"]),
        Mapping(target = "categoryId", source = "category", qualifiedByName = ["idFromCategory"]),
        Mapping(target = "purchaseLocationId", source = "purchaseLocation", qualifiedByName = ["idFromStore"])
    )
    fun toResult(entity: Product): ProductResult
}

// ------------------------------
// Movement
// ------------------------------

@Mapper(componentModel = "spring", uses = [IdRefMapper::class, EnumMapper::class], config = CommonMapperConfig::class)
interface MovementMapper {

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "user", source = "userId", qualifiedByName = ["userFromId"]),
        Mapping(target = "product", source = "productId", qualifiedByName = ["productFromId"]),
        Mapping(target = "store", source = "storeId", qualifiedByName = ["storeFromId"])
    )
    fun fromCreate(input: MovementCreate): Movement

    @Mappings(
        Mapping(target = "userId", source = "user", qualifiedByName = ["idFromUser"]),
        Mapping(target = "productId", source = "product", qualifiedByName = ["idFromProduct"]),
        Mapping(target = "storeId", source = "store", qualifiedByName = ["idFromStore"])
    )
    fun toResult(entity: Movement): MovementResult
}

// ------------------------------
// ShoppingItem
// ------------------------------

@Mapper(componentModel = "spring", uses = [IdRefMapper::class, EnumMapper::class], config = CommonMapperConfig::class)
interface ShoppingItemMapper {

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "user", source = "userId", qualifiedByName = ["userFromId"]),
        Mapping(target = "product", source = "productId", qualifiedByName = ["productFromId"]),
        Mapping(target = "purchased", constant = "false"),
        Mapping(target = "purchasedAt", ignore = true)
    )
    fun fromCreate(input: ShoppingItemCreate): ShoppingItem

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    fun update(input: ShoppingItemUpdate, @MappingTarget entity: ShoppingItem)

    @Mappings(
        Mapping(target = "userId", source = "user", qualifiedByName = ["idFromUser"]),
        Mapping(target = "productId", source = "product", qualifiedByName = ["idFromProduct"]),
        Mapping(target = "isPurchased", source = "purchased")
    )
    fun toResult(entity: ShoppingItem): ShoppingItemResult
}

// ------------------------------
// Alert
// ------------------------------

@Mapper(componentModel = "spring", uses = [IdRefMapper::class, EnumMapper::class], config = CommonMapperConfig::class)
interface AlertMapper {

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "user", source = "userId", qualifiedByName = ["userFromId"]),
        Mapping(target = "product", source = "productId", qualifiedByName = ["productFromId"]),
        Mapping(target = "active", source = "active")
    )
    fun fromCreate(input: AlertCreate): Alert

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    fun update(input: AlertUpdate, @MappingTarget entity: Alert)

    @Mappings(
        Mapping(target = "userId", source = "user", qualifiedByName = ["idFromUser"]),
        Mapping(target = "productId", source = "product", qualifiedByName = ["idFromProduct"]),
        Mapping(target = "isActive", source = "active")
    )
    fun toResult(entity: Alert): AlertResult
}

// ------------------------------
// PriceHistory
// ------------------------------

@Mapper(componentModel = "spring", uses = [IdRefMapper::class], config = CommonMapperConfig::class)
interface PriceHistoryMapper {

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "product", source = "productId", qualifiedByName = ["productFromId"]),
        Mapping(target = "store", source = "storeId", qualifiedByName = ["storeFromId"]),
        Mapping(target = "recordedAt", source = "recordedAt")
    )
    fun fromCreate(input: PriceHistoryCreate): PriceHistory

    @Mappings(
        Mapping(target = "productId", source = "product", qualifiedByName = ["idFromProduct"]),
        Mapping(target = "storeId", source = "store", qualifiedByName = ["idFromStore"])
    )
    fun toResult(entity: PriceHistory): PriceHistoryResult
}

// ------------------------------
// ProductRating
// ------------------------------

@Mapper(componentModel = "spring", uses = [IdRefMapper::class], config = CommonMapperConfig::class)
interface ProductRatingMapper {

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "user", source = "userId", qualifiedByName = ["userFromId"]),
        Mapping(target = "product", source = "productId", qualifiedByName = ["productFromId"])
    )
    fun fromCreate(input: ProductRatingCreate): ProductRating

    @Mappings(
        Mapping(target = "userId", source = "user", qualifiedByName = ["idFromUser"]),
        Mapping(target = "productId", source = "product", qualifiedByName = ["idFromProduct"])
    )
    fun toResult(entity: ProductRating): ProductRatingResult
}

// Comentario de cambios: agregado CommonMapperConfig y aplicado a todos los mappers
