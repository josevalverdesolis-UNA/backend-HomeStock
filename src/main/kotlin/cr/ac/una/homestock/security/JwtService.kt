package cr.ac.una.homestock.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*

@Service
class JwtService(
    @Value("\${app.jwt.secret}") private val secret: String,
    @Value("\${app.jwt.issuer:homestock}") private val issuer: String,
    @Value("\${app.jwt.expiration-minutes:60}") private val expirationMinutes: Long
) {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    fun generateToken(userId: Long, email: String, role: String): String {
        val now = Instant.now()
        val exp = now.plusSeconds(expirationMinutes * 60)
        return Jwts.builder()
            .subject(userId.toString())
            .issuer(issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claim("email", email)
            .claim("role", role)
            .signWith(key)
            .compact()
    }

    fun parseAndValidate(token: String) =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
}