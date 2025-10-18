package cr.ac.una.homestock.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:Email @field:NotBlank
    val email: String,
    @field:NotBlank @field:Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    val password: String
)

data class LoginRequest(
    @field:Email @field:NotBlank
    val email: String,
    @field:NotBlank
    val password: String
)

data class AuthResponse(
    val token: String
)