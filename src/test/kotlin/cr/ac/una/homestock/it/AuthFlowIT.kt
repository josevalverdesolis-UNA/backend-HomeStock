package cr.ac.una.homestock.it

import cr.ac.una.homestock.auth.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFlowIT {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setupDb() {
            try {
                val clazz = Class.forName("org.testcontainers.containers.PostgreSQLContainer")
                val ctor = clazz.getConstructor(String::class.java)
                val instance = ctor.newInstance("postgres:16-alpine")
                val startMethod = clazz.getMethod("start")
                startMethod.invoke(instance)
                val getJdbcUrl = clazz.getMethod("getJdbcUrl")
                val getUsername = clazz.getMethod("getUsername")
                val getPassword = clazz.getMethod("getPassword")
                val url = getJdbcUrl.invoke(instance) as String
                val user = getUsername.invoke(instance) as String
                val pass = getPassword.invoke(instance) as String
                System.setProperty("spring.datasource.url", url)
                System.setProperty("spring.datasource.username", user)
                System.setProperty("spring.datasource.password", pass)
            } catch (_: ClassNotFoundException) {
                // Testcontainers no disponible: el test correrá con la config por defecto
            }
        }
    }

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    lateinit var rest: TestRestTemplate

    private fun url(p: String) = "http://localhost:$port$p"

    @Test
    fun `register login me refresh logout`() {
        val email = "user${System.currentTimeMillis()}@test.local"
        val password = "StrongPass123!"
        val name = "Test User"

        // Register
        val regReq = RegisterRequest(email = email, password = password, name = name)
        val regResp = rest.postForEntity(url("/api/v1/auth/register"), regReq, RegisterResponse::class.java)
        assertEquals(HttpStatus.CREATED, regResp.statusCode)
        val user = regResp.body!!
        assertEquals(email.lowercase(), user.email)
        assertEquals(name, user.name)
        assertNotNull(user.id)
        assertNotNull(user.createdAt)

        // Login
        val loginReq = LoginRequest(email = email, password = password)
        val loginResp = rest.postForEntity(url("/api/v1/auth/login"), loginReq, AuthResponse::class.java)
        assertEquals(HttpStatus.OK, loginResp.statusCode)
        val auth = loginResp.body!!
        assertTrue(auth.accessToken.isNotBlank())
        assertTrue(auth.refreshToken.isNotBlank())
        assertTrue(auth.expiresIn > 0)
        assertEquals(user.id, auth.user.id)
        assertEquals(user.email, auth.user.email)
        assertEquals(user.name, auth.user.name)

        // Me
        val headers = HttpHeaders()
        headers.setBearerAuth(auth.accessToken)
        val meEntity = rest.exchange(url("/api/v1/auth/me"), HttpMethod.GET, HttpEntity<Void>(headers), MeResponse::class.java)
        assertEquals(HttpStatus.OK, meEntity.statusCode)
        assertEquals(user.id, meEntity.body!!.id)
        assertEquals(user.email, meEntity.body!!.email)
        assertEquals(user.name, meEntity.body!!.name)

        // Refresh (rotates refresh token)
        val refreshReq = RefreshRequest(refreshToken = auth.refreshToken)
        val refreshEntity = rest.postForEntity(url("/api/v1/auth/refresh"), refreshReq, AccessTokenResponse::class.java)
        assertEquals(HttpStatus.OK, refreshEntity.statusCode)
        val newAccess = refreshEntity.body!!.accessToken
        assertTrue(newAccess.isNotBlank())
        val newRefresh = refreshEntity.headers.getFirst("X-Refresh-Token")
        assertNotNull(newRefresh)

        // Logout (revoke latest refresh)
        val logoutReq = RefreshRequest(refreshToken = newRefresh!!)
        val logoutResp = rest.postForEntity(url("/api/v1/auth/logout"), logoutReq, Void::class.java)
        assertEquals(HttpStatus.NO_CONTENT, logoutResp.statusCode)
    }
}
