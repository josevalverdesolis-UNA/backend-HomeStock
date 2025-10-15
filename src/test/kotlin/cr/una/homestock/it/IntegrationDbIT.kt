package cr.una.homestock.it


import cr.una.homestock.repository.*
import cr.una.homestock.domain.model.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Sql(
    scripts = ["/sql/clean_all.sql", "/sql/insert_all.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Transactional(propagation = Propagation.NOT_SUPPORTED) // evita rollback por método
class IntegrationDbIT(
    @Autowired val userRepo: UserRepository,
    @Autowired val categoryRepo: CategoryRepository,
    @Autowired val storeRepo: StoreRepository,
    @Autowired val productRepo: ProductRepository,
    @Autowired val movementRepo: MovementRepository,
    @Autowired val shoppingRepo: ShoppingItemRepository,
    @Autowired val alertRepo: AlertRepository,
    @Autowired val priceRepo: PriceHistoryRepository,
    @Autowired val ratingRepo: ProductRatingRepository
) {

    @Test @Order(1)
    fun `debe existir un usuario, categoria, tienda y producto`() {
        assertThat(userRepo.count()).isEqualTo(1)
        assertThat(categoryRepo.count()).isEqualTo(1)
        assertThat(storeRepo.count()).isEqualTo(1)
        assertThat(productRepo.count()).isEqualTo(1)

        val p = productRepo.findAll().first()
        assertThat(p.name).isEqualTo("Arroz 1kg")
        assertThat(p.minStock).isEqualTo(3)
    }

    @Test @Order(2)
    fun `debe existir movimientos, shopping item, alerta, historial de precios y rating`() {
        assertThat(movementRepo.count()).isEqualTo(2)
        assertThat(shoppingRepo.count()).isEqualTo(1)
        assertThat(alertRepo.count()).isEqualTo(1)
        assertThat(priceRepo.count()).isEqualTo(1)
        assertThat(ratingRepo.count()).isEqualTo(1)
    }

    @Test @Order(3)
    fun `consulta de negocio - movimientos por producto`() {
        val productId = "44444444-4444-4444-4444-444444444444"
        val list = movementRepo.findByProductIdOrderByOccurredAtDesc(productId)
        assertThat(list).isNotEmpty()
        // Debe estar primero el CONSUMPTION de "hoy"
        assertThat(list.first().type).isEqualTo(MovementType.CONSUMPTION)
    }

    @Test @Order(4)
    fun `puedo actualizar stock y dejar persistido`() {
        val p = productRepo.findAll().first()
        val prev = p.quantity
        p.quantity = prev + 5
        productRepo.save(p)

        val again = productRepo.findById(p.id!!).orElseThrow()
        assertThat(again.quantity).isEqualTo(prev + 5)
    }
}
