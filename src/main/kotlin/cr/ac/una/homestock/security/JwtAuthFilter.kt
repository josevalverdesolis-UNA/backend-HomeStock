package cr.ac.una.homestock.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwt: JwtService
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header?.startsWith("Bearer ") == true) {
            val token = header.removePrefix("Bearer ").trim()
            try {
                val claims = jwt.parseAndValidate(token)
                val userId = claims.subject // id como String
                val role = claims.get("role", String::class.java)?.uppercase() ?: "USER"
                val auth = UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_$role"))
                )
                SecurityContextHolder.getContext().authentication = auth
            } catch (_: Exception) {
                // Token inválido/expirado: no autentica; seguirá como anónimo.
            }
        }
        chain.doFilter(request, response)
    }
}