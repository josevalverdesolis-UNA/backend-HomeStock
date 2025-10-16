package cr.ac.una.homestock.it

import cr.ac.una.homestock.domain.entity.Category
import cr.ac.una.homestock.domain.entity.Product
import cr.ac.una.homestock.domain.entity.Store
import cr.ac.una.homestock.domain.entity.User
import cr.ac.una.homestock.repository.CategoryRepository
import cr.ac.una.homestock.repository.ProductRepository
import cr.ac.una.homestock.repository.StoreRepository
import cr.ac.una.homestock.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "spring.flyway.baseline-on-migrate=true",
    "spring.flyway.validate-on-migrate=false"
])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class IntegrationDbIT {

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var categoryRepository: CategoryRepository
    @Autowired lateinit var storeRepository: StoreRepository
    @Autowired lateinit var productRepository: ProductRepository

    @Test
    @Sql(scripts = ["classpath:sql/clean_all.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    fun shouldPerformCrudOnProduct() {
        // Datos únicos para evitar colisiones de restricciones únicas
        val suffix = System.currentTimeMillis().toString()

        // Create: User, Category, Store
        val user = userRepository.save(User(name = "Tester$suffix", email = "tester$suffix@example.com"))
        val category = categoryRepository.save(Category(name = "Cat$suffix", description = "Cat desc $suffix"))
        val store = storeRepository.save(Store(name = "Store$suffix", location = "Loc", notes = null))

        // Create: Product
        var product = Product(
            user = user,
            name = "Prod$suffix",
            category = category,
            quantity = 2,
            minStock = 1,
            expiryDate = LocalDate.now().plusDays(30),
            price = BigDecimal("123.45"),
            purchaseLocation = store,
            brand = "Brand$suffix",
            imageUrl = null
        )
        product = productRepository.save(product)
        assertThat(product.id).isNotNull()

        // Read
        val loaded = productRepository.findById(product.id!!)
        assertThat(loaded).isPresent
        assertThat(loaded.get().name).isEqualTo("Prod$suffix")

        // Update
        val toUpdate = loaded.get()
        toUpdate.quantity = 5
        toUpdate.minStock = 2
        toUpdate.price = BigDecimal("150.00")
        val updated = productRepository.save(toUpdate)
        assertThat(updated.quantity).isEqualTo(5)
        assertThat(updated.minStock).isEqualTo(2)
        assertThat(updated.price).isEqualByComparingTo(BigDecimal("150.00"))

        // Delete
        productRepository.deleteById(updated.id!!)
        val afterDelete = productRepository.findById(updated.id!!)
        assertThat(afterDelete).isNotPresent
    }
}
