package cr.ac.una.homestock.auth

import cr.ac.una.homestock.repository.UserRepository
import cr.ac.una.homestock.security.JwtService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val users: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwt: JwtService
) {
    @Transactional
    fun register(req: RegisterRequest) {
        val email = req.email.lowercase()
        if (users.findByEmail(email).isPresent) {
            throw DataIntegrityViolationException("El email ya está registrado")
        }
        // Usa tu entidad User existente
        val entity = cr.ac.una.homestock.domain.entity.User(
            // Ajusta constructores según tu entidad
            email = email,
        ).apply {
            passwordHash = passwordEncoder.encode(req.password)
            // role por defecto = "USER"
        }
        users.save(entity)
    }

    @Transactional(readOnly = true)
    fun login(req: LoginRequest): AuthResponse {
        val user = users.findByEmail(req.email.lowercase())
            .orElseThrow { IllegalArgumentException("Credenciales inválidas") }

        if (!passwordEncoder.matches(req.password, user.passwordHash)) {
            throw IllegalArgumentException("Credenciales inválidas")
        }

        val role = user.role // default "USER"
        val token = jwt.generateToken(
            userId = user.id!!,
            email = user.email,
            role = role
        )
        return AuthResponse(token)
    }
}