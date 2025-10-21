package cr.ac.una.homestock.auth

import cr.ac.una.homestock.domain.entity.RefreshToken
import cr.ac.una.homestock.repository.RefreshTokenRepository
import cr.ac.una.homestock.repository.UserRepository
import cr.ac.una.homestock.security.JwtService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant

@Service
class AuthService(
    private val users: UserRepository,
    private val refreshRepo: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwt: JwtService
) {
    // ------------------ Register ------------------
    @Transactional
    fun register(req: RegisterRequest): RegisterResponse {
        val email = req.email.trim().lowercase()
        if (users.findByEmail(email).isPresent) {
            throw DataIntegrityViolationException("El email ya está registrado")
        }
        val entity = cr.ac.una.homestock.domain.entity.User(
            name = req.name.trim(),
            email = email,
            passwordHash = passwordEncoder.encode(req.password),
            role = "USER"
        )
        val saved = users.save(entity)
        return saved.toRegisterResponse()
    }

    // ------------------ Login ------------------
    @Transactional
    fun login(req: LoginRequest): AuthResponse {
        val user = users.findByEmail(req.email.trim().lowercase())
            .orElseThrow { IllegalArgumentException("Credenciales inválidas") }

        if (!passwordEncoder.matches(req.password, user.passwordHash)) {
            throw IllegalArgumentException("Credenciales inválidas")
        }

        // Revocar refresh tokens previos activos
        refreshRepo.findAllByUser_IdAndRevokedAtIsNull(user.id!!).forEach { it.revokedAt = Instant.now() }

        val (access, _) = jwt.generateAccessToken(user.id!!, user.email, user.role)
        val (refresh, refreshExp) = jwt.generateRefreshToken(user.id!!, user.email)

        // Guardar refresh hash
        val tokenHash = sha256Hex(refresh)
        refreshRepo.save(RefreshToken(user = user, tokenHash = tokenHash, expiresAt = refreshExp, createdAt = Instant.now()))

        return AuthResponse(
            accessToken = access,
            refreshToken = refresh,
            expiresIn = jwt.getAccessTtlSeconds(),
            user = user.toUserSummary()
        )
    }

    data class RefreshOutcome(
        val response: AccessTokenResponse,
        val newRefreshToken: String
    )

    // ------------------ Refresh ------------------
    @Transactional
    fun refresh(req: RefreshRequest): RefreshOutcome {
        val token = req.refreshToken.trim()
        val claims = jwt.parseAndValidate(token)
        val typ = claims["typ"] as? String
        require(typ == "refresh") { "Token inválido" }
        val userId = claims.subject?.toLongOrNull() ?: throw IllegalArgumentException("Token inválido")
        val exp = claims.expiration?.toInstant() ?: Instant.EPOCH
        if (exp.isBefore(Instant.now())) throw IllegalArgumentException("Refresh token expirado")

        val tokenHash = sha256Hex(token)
        val stored = refreshRepo.findByTokenHash(tokenHash).orElseThrow { IllegalArgumentException("Refresh token no reconocido") }
        if (stored.user?.id != userId) throw IllegalArgumentException("Token inválido")
        if (stored.revokedAt != null) throw IllegalArgumentException("Refresh token revocado")
        if (stored.expiresAt.isBefore(Instant.now())) throw IllegalArgumentException("Refresh token expirado")

        val user = stored.user!!
        val (access, _) = jwt.generateAccessToken(user.id!!, user.email, user.role)

        // Rotación: revoca el actual y emite uno nuevo
        stored.revokedAt = Instant.now()
        val (newRefresh, newExp) = jwt.generateRefreshToken(user.id!!, user.email)
        val newHash = sha256Hex(newRefresh)
        refreshRepo.save(RefreshToken(user = user, tokenHash = newHash, expiresAt = newExp, createdAt = Instant.now()))

        return RefreshOutcome(
            response = AccessTokenResponse(accessToken = access, expiresIn = jwt.getAccessTtlSeconds()),
            newRefreshToken = newRefresh
        )
    }

    // ------------------ Logout ------------------
    @Transactional
    fun logout(req: RefreshRequest) {
        val token = req.refreshToken.trim()
        val tokenHash = sha256Hex(token)
        val stored = refreshRepo.findByTokenHash(tokenHash).orElse(null) ?: return
        if (stored.revokedAt == null) stored.revokedAt = Instant.now()
    }

    // ------------------ Me ------------------
    @Transactional(readOnly = true)
    fun me(): MeResponse {
        val auth = SecurityContextHolder.getContext().authentication
        val userId = (auth?.principal as? String)?.toLongOrNull() ?: throw IllegalArgumentException("No autenticado")
        val user = users.findById(userId).orElseThrow { IllegalArgumentException("Usuario no encontrado") }
        return user.toMeResponse()
    }

    // Utils
    private fun sha256Hex(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

private fun cr.ac.una.homestock.domain.entity.User.toRegisterResponse() =
    RegisterResponse(
        id = this.id!!,
        email = this.email,
        name = this.name,
        createdAt = this.createdAt
    )

private fun cr.ac.una.homestock.domain.entity.User.toUserSummary() =
    UserSummary(
        id = this.id!!,
        email = this.email,
        name = this.name
    )

private fun cr.ac.una.homestock.domain.entity.User.toMeResponse() =
    MeResponse(
        id = this.id!!,
        email = this.email,
        name = this.name,
        households = emptyList(),
        preferences = emptyMap()
    )
