package cr.ac.una.homestock.mapper

import cr.ac.una.homestock.dto.*
import cr.ac.una.homestock.data.*
import cr.ac.una.homestock.domain.model.*
import org.mapstruct.*
import java.util.*

/* ============================================================================
 * Configuración base
 * ============================================================================
 */
@MapperConfig(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface BaseMapperConfig

/* ============================================================================
 * USER
 * ============================================================================
 */
@Mapper(config = BaseMapperConfig::class)
interface UserMapper {
    fun toDomain(entity: UserEntity): User
    fun toEntity(domain: User): UserEntity
    fun toDto(domain: User): UserDto
    fun toDomain(dto: UserDto): User
}

/* ============================================================================
 * CATEGORY
 * ============================================================================
 */
@Mapper(config = BaseMapperConfig::class)
interface CategoryMapper {
    fun toDomain(entity: CategoryEntity): Category
    fun toEntity(domain: Category): CategoryEntity
    fun toDto(domain: Category): CategoryDto
    fun toDomain(dto: CategoryDto): Category
}

/* ============================================================================
 * STORE
 * ============================================================================
 */
@Mapper(config = BaseMapperConfig::class)
interface StoreMapper {
    fun toDomain(entity: StoreEntity): Store
    fun toEntity(domain: Store): StoreEntity
    fun toDto(domain: Store): StoreDto
    fun toDomain(dto: StoreDto): Store
}

/* ============================================================================
 * PRODUCT
 * ============================================================================
 */
@Mapper(config = BaseMapperConfig::class)
interface ProductMapper {
    /* ---- Entity ↔ Domain ---- */
    @Mappings(
        Mapping(target = "userId", source = "entity.user.id"),
        Mapping(target = "categoryId", source = "entity.category.id"),
        Mapping(target = "purchaseLocationId", source = "entity.purchaseLocation.id")
    )
    fun toDomain(entity: ProductEntity): Product

    @InheritInverseConfiguration
    @Mappings(
        Mapping(target = "user", expression = "java(toUserEntity(domain.getUserId()))"),
        Mapping(target = "category", expression = "java(toCategoryEntity(domain.getCategoryId()))"),
        Mapping(target = "purchaseLocation", expression = "java(toStoreEntity(domain.getPurchaseLocationId()))")
    )
    fun toEntity(domain: Product): ProductEntity

    /* ---- Domain ↔ DTO ---- */
    fun toDto(domain: Product): ProductDto
    fun toDomain(dto: ProductDto): Product

    fun toDtoList(domains: List<Product>): List<ProductDto>

    /* ---- Helper (IDs → Entities vacíos) ---- */
    fun toUserEntity(id: String?): UserEntity? = id?.let { UserEntity(id = it, name = "", email = "") }
    fun toCategoryEntity(id: UUID?): CategoryEntity? = id?.let { CategoryEntity(id = it, name = "") }
    fun toStoreEntity(id: UUID?): StoreEntity? = id?.let { StoreEntity(id = it, name = "") }
}

/* ============================================================================
 * MOVEMENT
 * ============================================================================
 */
@Mapper(config = BaseMapperConfig::class)
interface MovementMapper {
    @Mappings(
        Mapping(target = "productId", source = "entity.product.id"),
        Mapping(target = "userId", source = "entity.user.id"),
        Mapping(target = "storeId", source = "entity.store.id")
    )
    fun toDomain(entity: MovementEntity): Movement

    @InheritInverseConfiguration
    @Mappings(
        Mapping(target = "product", expression = "java(toProductEntity(domain.getProductId()))"),
        Mapping(target = "user", expression = "java(toUserEntity(domain.getUserId()))"),
        Mapping(target = "store", expression = "java(toStoreEntity(domain.getStoreId()))")
    )
    fun toEntity(domain: Movement): MovementEntity

    fun toDto(domain: Movement): MovementDto
    fun toDomain(dto: MovementDto): Movement

    /* Helpers */
    fun toProductEntity(id: UUID?): ProductEntity? = id?.let {
        ProductEntity(id = it, user = UserEntity("","", ""), category = CategoryEntity(name = ""), name = "")
    }
    fun toUserEntity(id: String?): UserEntity? = id?.let { UserEntity(id = it, name = "", email = "") }
    fun toStoreEntity(id: UUID?): StoreEntity? = id?.let { StoreEntity(id = it, name = "") }
}

/* ============================================================================
 * SHOPPING ITEM
 * ============================================================================
 */
@Mapper(config = BaseMapperConfig::class)
interface ShoppingItemMapper {
    @Mappings(
        Mapping(target = "userId", source = "entity.user.id"),
        Mapping(target = "productId", source = "entity.product.id"),
        Mapping(target = "targetStoreId", source = "entity.targetStore.id")
    )
    fun toDomain(entity: ShoppingItemEntity): ShoppingItem

    @InheritInverseConfiguration
    @Mappings(
        Mapping(target = "user", expression = "java(toUserEntity(domain.getUserId()))"),
        Mapping(target = "product", expression = "java(toProductEntity(domain.getProductId()))"),
        Mapping(target = "targetStore", expression = "java(toStoreEntity(domain.getTargetStoreId()))")
    )
    fun toEntity(domain: ShoppingItem): ShoppingItemEntity

    fun toDto(domain: ShoppingItem): ShoppingItemDto
    fun toDomain(dto: ShoppingItemDto): ShoppingItem

    /* Helpers */
    fun toUserEntity(id: String?): UserEntity? = id?.let { UserEntity(id = it, name = "", email = "") }
    fun toProductEntity(id: UUID?): ProductEntity? = id?.let {
        ProductEntity(id = it, user = UserEntity("","", ""), category = CategoryEntity(name = ""), name = "")
    }
    fun toStoreEntity(id: UUID?): StoreEntity? = id?.let { StoreEntity(id = it, name = "") }
}

/* ============================================================================
 * ALERT
 * ============================================================================
 */
@Mapper(config = BaseMapperConfig::class)
interface AlertMapper {
    @Mappings(
        Mapping(target = "userId", source = "entity.user.id"),
        Mapping(target = "productId", source = "entity.product.id")
    )
    fun toDomain(entity: AlertEntity): Alert

    @InheritInverseConfiguration
    @Mappings(
        Mapping(target = "user", expression = "java(toUserEntity(domain.getUserId()))"),
        Mapping(target = "product", expression = "java(toProductEntity(domain.getProductId()))")
    )
    fun toEntity(domain: Alert): AlertEntity

    fun toDto(domain: Alert): AlertDto
    fun toDomain(dto: AlertDto): Alert

    fun toUserEntity(id: String?): UserEntity? = id?.let { UserEntity(id = it, name = "", email = "") }
    fun toProductEntity(id: UUID?): ProductEntity? = id?.let {
        ProductEntity(id = it, user = UserEntity("","", ""), category = CategoryEntity(name = ""), name = "")
    }
}

/* ============================================================================
 * PRICE HISTORY
 * ============================================================================
 */
@Mapper(config = BaseMapperConfig::class)
interface PriceHistoryMapper {
    @Mappings(
        Mapping(target = "productId", source = "entity.product.id"),
        Mapping(target = "storeId", source = "entity.store.id")
    )
    fun toDomain(entity: PriceHistoryEntity): PriceHistory

    @InheritInverseConfiguration
    @Mappings(
        Mapping(target = "product", expression = "java(toProductEntity(domain.getProductId()))"),
        Mapping(target = "store", expression = "java(toStoreEntity(domain.getStoreId()))")
    )
    fun toEntity(domain: PriceHistory): PriceHistoryEntity

    fun toDto(domain: PriceHistory): PriceHistoryDto
    fun toDomain(dto: PriceHistoryDto): PriceHistory

    fun toProductEntity(id: UUID?): ProductEntity? = id?.let {
        ProductEntity(id = it, user = UserEntity("","", ""), category = CategoryEntity(name = ""), name = "")
    }
    fun toStoreEntity(id: UUID?): StoreEntity? = id?.let { StoreEntity(id = it, name = "") }
}

/* ============================================================================
 * PRODUCT RATING
 * ============================================================================
 */
@Mapper(config = BaseMapperConfig::class)
interface ProductRatingMapper {
    @Mappings(
        Mapping(target = "userId", source = "entity.user.id"),
        Mapping(target = "productId", source = "entity.product.id")
    )
    fun toDomain(entity: ProductRatingEntity): ProductRating

    @InheritInverseConfiguration
    @Mappings(
        Mapping(target = "user", expression = "java(toUserEntity(domain.getUserId()))"),
        Mapping(target = "product", expression = "java(toProductEntity(domain.getProductId()))")
    )
    fun toEntity(domain: ProductRating): ProductRatingEntity

    fun toDto(domain: ProductRating): ProductRatingDto
    fun toDomain(dto: ProductRatingDto): ProductRating

    fun toUserEntity(id: String?): UserEntity? = id?.let { UserEntity(id = it, name = "", email = "") }
    fun toProductEntity(id: UUID?): ProductEntity? = id?.let {
        ProductEntity(id = it, user = UserEntity("","", ""), category = CategoryEntity(name = ""), name = "")
    }
}
