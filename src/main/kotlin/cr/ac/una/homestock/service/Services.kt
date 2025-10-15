package cr.ac.una.homestock.service


import cr.ac.una.homestock.dto.*
import cr.ac.una.homestock.data.*
import cr.ac.una.homestock.domain.model.Product
import cr.ac.una.homestock.domain.model.Category
import cr.ac.una.homestock.mapper.ProductMapper
import cr.ac.una.homestock.mapper.CategoryMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import org.springframework.dao.DataIntegrityViolationException

class NotFoundException(message: String) : RuntimeException(message)

@Service
class ProductService(
    private val productRepo: ProductJpaRepository,
    private val userRepo: UserJpaRepository,
    private val categoryRepo: CategoryJpaRepository,
    private val storeRepo: StoreJpaRepository,
    private val mapper: ProductMapper
) {
    fun list(userId: String? = null, q: String? = null, categoryId: UUID? = null): List<ProductDto> {
        val entities = when {
            userId != null && categoryId != null -> productRepo.findByUser_IdAndCategory_Id(userId, categoryId)
            userId != null -> productRepo.findByUser_Id(userId)
            q != null && q.isNotBlank() -> productRepo.findByNameContainingIgnoreCase(q)
            else -> productRepo.findAll()
        }
        val domains: List<Product> = entities.map { mapper.toDomain(it) }
        return mapper.toDtoList(domains)
    }

    fun get(id: UUID): ProductDto {
        val entity = productRepo.findById(id).orElseThrow { NotFoundException("Product $id not found") }
        return mapper.toDto(mapper.toDomain(entity))
    }

    @Transactional
    fun create(body: CreateProductDto): ProductDto {
        val user = userRepo.findById(body.userId).orElseThrow { NotFoundException("User ${body.userId} not found") }
        val category = categoryRepo.findById(body.categoryId!!).orElseThrow { NotFoundException("Category ${body.categoryId} not found") }
        val store = body.purchaseLocationId?.let { id -> storeRepo.findById(id).orElse(null) }

        val entity = ProductEntity(
            user = user,
            category = category,
            name = body.name,
            brand = body.brand,
            quantity = body.quantity,
            minStock = body.minStock,
            acquisitionDate = body.acquisitionDate,
            expiryDate = body.expiryDate,
            price = body.price,
            purchaseLocation = store,
            imageUrl = body.imageUrl
        )
        val saved = productRepo.save(entity)
        return mapper.toDto(mapper.toDomain(saved))
    }

    @Transactional
    fun update(id: UUID, body: UpdateProductDto): ProductDto {
        val e = productRepo.findById(id).orElseThrow { NotFoundException("Product $id not found") }

        body.categoryId?.let { catId -> e.category = categoryRepo.findById(catId).orElseThrow { NotFoundException("Category $catId not found") } }
        body.name?.let { e.name = it }
        body.brand?.let { e.brand = it }
        body.quantity?.let { e.quantity = it }
        body.minStock?.let { e.minStock = it }
        body.acquisitionDate?.let { e.acquisitionDate = it }
        body.expiryDate?.let { e.expiryDate = it }
        body.price?.let { e.price = it }
        if (body.purchaseLocationId != null) {
            e.purchaseLocation = storeRepo.findById(body.purchaseLocationId).orElseThrow { NotFoundException("Store ${body.purchaseLocationId} not found") }
        }
        body.imageUrl?.let { e.imageUrl = it }

        val saved = productRepo.save(e)
        return mapper.toDto(mapper.toDomain(saved))
    }

    @Transactional
    fun delete(id: UUID) {
        if (!productRepo.existsById(id)) throw NotFoundException("Product $id not found")
        productRepo.deleteById(id)
    }
}

@Service
class CategoryService(
    private val categoryRepo: CategoryJpaRepository,
    private val mapper: CategoryMapper
) {
    fun list(): List<CategoryDto> = categoryRepo.findAll()
        .map { mapper.toDomain(it) }
        .map { mapper.toDto(it) }

    fun get(id: UUID): CategoryDto {
        val e = categoryRepo.findById(id).orElseThrow { NotFoundException("Category $id not found") }
        return mapper.toDto(mapper.toDomain(e))
    }

    @Transactional
    fun create(body: CreateCategoryDto): CategoryDto {
        if (categoryRepo.existsByNameIgnoreCase(body.name)) {
            throw DataIntegrityViolationException("Category name already exists: ${body.name}")
        }
        val saved = categoryRepo.save(CategoryEntity(name = body.name))
        return mapper.toDto(mapper.toDomain(saved))
    }

    @Transactional
    fun update(id: UUID, body: UpdateCategoryDto): CategoryDto {
        val e = categoryRepo.findById(id).orElseThrow { NotFoundException("Category $id not found") }
        body.name?.let { newName ->
            if (!newName.equals(e.name, ignoreCase = true) && categoryRepo.existsByNameIgnoreCase(newName)) {
                throw DataIntegrityViolationException("Category name already exists: $newName")
            }
            e.name = newName
        }
        val saved = categoryRepo.save(e)
        return mapper.toDto(mapper.toDomain(saved))
    }

    @Transactional
    fun delete(id: UUID) {
        if (!categoryRepo.existsById(id)) throw NotFoundException("Category $id not found")
        categoryRepo.deleteById(id)
    }
}
