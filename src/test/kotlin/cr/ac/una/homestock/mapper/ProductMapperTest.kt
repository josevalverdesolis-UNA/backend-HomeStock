package cr.ac.una.homestock.mapper

import cr.ac.una.homestock.data.entity.ProductEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ProductMapperTest(
    @Autowired private val mapper: ProductMapper
) {
    @Test
    fun `mapear entity a dominio y dto`() {
        val entity = ProductEntity(id = 1L, name = "Frijoles", quantity = 5)
        val domain = mapper.toDomain(entity)
        val dto = mapper.toDto(domain)
        assertEquals("Frijoles", dto.name)
        assertEquals(5, dto.quantity)
    }
}

