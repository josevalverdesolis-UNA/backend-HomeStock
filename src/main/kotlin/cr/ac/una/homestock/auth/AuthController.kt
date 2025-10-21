@file:Suppress("unused")
package cr.ac.una.homestock.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Suppress("unused")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val service: AuthService
) {
    @Operation(summary = "Registro de usuario")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Usuario creado",
            content = [Content(schema = Schema(implementation = RegisterResponse::class),
                examples = [ExampleObject(value = "{\\n  'id': 1,\\n  'email': 'john@doe.com',\\n  'name': 'John Doe',\\n  'createdAt': '2025-01-01T00:00:00Z'\\n}")])]        ),
        ApiResponse(responseCode = "409", description = "Email ya registrado")
    )
    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest): ResponseEntity<RegisterResponse> {
        val user = service.register(req)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }

    @Operation(summary = "Login con email y contraseña")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Autenticado",
            content = [Content(schema = Schema(implementation = AuthResponse::class))]
        ),
        ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    )
    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): AuthResponse =
        service.login(req)

    @Operation(summary = "Refresca el access token y rota el refresh token")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Token renovado",
            content = [Content(schema = Schema(implementation = AccessTokenResponse::class))]
        ),
        ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
    )
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody req: RefreshRequest): ResponseEntity<AccessTokenResponse> {
        val outcome = service.refresh(req)
        return ResponseEntity.ok()
            .header("X-Refresh-Token", outcome.newRefreshToken)
            .body(outcome.response)
    }

    @Operation(summary = "Cierra sesión revocando el refresh token")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Logout exitoso")
    )
    @PostMapping("/logout")
    fun logout(@Valid @RequestBody req: RefreshRequest): ResponseEntity<Void> {
        service.logout(req)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Obtiene el perfil del usuario autenticado")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Perfil",
            content = [Content(schema = Schema(implementation = MeResponse::class))]
        ),
        ApiResponse(responseCode = "401", description = "No autenticado")
    )
    @GetMapping("/me")
    fun me(): MeResponse = service.me()
}