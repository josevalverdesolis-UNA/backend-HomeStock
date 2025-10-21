@file:Suppress("unused")
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
    @Value("\${app.jwt.access-ttl-seconds:900}") private val accessTtlSeconds: Long,
    @Value("\${app.jwt.refresh-ttl-seconds:1209600}") private val refreshTtlSeconds: Long,
) {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    fun generateAccessToken(userId: Long, email: String, role: String): Pair<String, Instant> {
        val now = Instant.now()
        val exp = now.plusSeconds(accessTtlSeconds)
        val token = Jwts.builder()
            .subject(userId.toString())
            .issuer(issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claim("email", email)
            .claim("role", role)
            .claim("typ", "access")
            .signWith(key)
            .compact()
        return token to exp
    }

    fun generateRefreshToken(userId: Long, email: String): Pair<String, Instant> {
        val now = Instant.now()
        val exp = now.plusSeconds(refreshTtlSeconds)
        val token = Jwts.builder()
            .subject(userId.toString())
            .issuer(issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claim("email", email)
            .claim("typ", "refresh")
            .signWith(key)
            .compact()
        return token to exp
    }

    fun parseAndValidate(token: String) =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload

    fun getAccessTtlSeconds(): Long = accessTtlSeconds
    fun getRefreshTtlSeconds(): Long = refreshTtlSeconds
}