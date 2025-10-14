package cr.ac.una.homestock.service

import cr.ac.una.homestock.dto.ProductDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceTest(
    @Autowired private val service: ProductService
) {
    @Test
    fun `crear y obtener producto`() {
        val created = service.create(ProductDto(name = "Arroz", quantity = 2))
        assertNotNull(created.id)
        val fetched = service.getById(created.id!!)
        assertEquals("Arroz", fetched?.name)
        assertEquals(2, fetched?.quantity)
    }
}

