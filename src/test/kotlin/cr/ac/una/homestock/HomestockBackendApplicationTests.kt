package cr.ac.una.homestock

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test") // Usa el perfil de pruebas con H2
class HomestockBackendApplicationTests {

	@Test
	fun contextLoads() {
	}

}
