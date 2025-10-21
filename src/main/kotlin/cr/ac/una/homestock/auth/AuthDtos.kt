package cr.ac.una.homestock.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

// Requests

data class RegisterRequest(
    @field:Email @field:NotBlank
    val email: String,
    @field:NotBlank @field:Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    val password: String,
    @field:NotBlank
    val name: String
)

data class LoginRequest(
    @field:Email @field:NotBlank
    val email: String,
    @field:NotBlank
    val password: String
)

data class RefreshRequest(
    @field:NotBlank
    val refreshToken: String
)

// Responses exactas por endpoint

data class RegisterResponse(
    val id: Long,
    val email: String,
    val name: String,
    val createdAt: Instant
)

data class UserSummary(
    val id: Long,
    val email: String,
    val name: String
)

data class MeResponse(
    val id: Long,
    val email: String,
    val name: String,
    val households: List<Any>,
    val preferences: Map<String, Any?>
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserSummary
)

data class AccessTokenResponse(
    val accessToken: String,
    val expiresIn: Long
)