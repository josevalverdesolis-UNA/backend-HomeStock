package cr.ac.una.homestock.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { } // usa el bean corsConfigurationSource
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                // Swagger/actuator opcional:
                it.requestMatchers(
                    "/swagger-ui/**", "/v3/api-docs/**", "/v1/api-docs/**", "/actuator/health"
                ).permitAll()

                // Auth público
                it.requestMatchers("/api/v1/auth/**").permitAll()

                // Otros GET públicos si así lo deseas (opcional)
                // it.requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()

                // El resto requiere JWT
                it.anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val cfg = CorsConfiguration()
        cfg.allowedOriginPatterns = listOf("*") // Android no requiere CORS, pero si expones web admin cambia esto
        cfg.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        cfg.allowedHeaders = listOf("*")
        cfg.allowCredentials = false

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", cfg)
        return source
    }
}