package cr.ac.una.homestock.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero

data class ProductDto(
    val id: Long? = null,
    @field:NotBlank(message = "El nombre no puede estar en blanco")
    val name: String,
    @field:PositiveOrZero(message = "La cantidad debe ser >= 0")
    val quantity: Int
)

