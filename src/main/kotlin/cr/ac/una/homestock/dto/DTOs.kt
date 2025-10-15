package cr.una.homestock.domain.dto

import cr.una.homestock.domain.model.MovementType
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

/* =========================
   PRODUCT
   ========================= */

data class ProductInput(
    @field:NotBlank
    val userId: String,
    val categoryId: String? = null,
    val storeId: String? = null,
    @field:NotBlank
    val name: String,
    val brand: String? = null,
    @field:Min(0)
    val quantity: Int = 0,
    @field:Min(0)
    val minStock: Int = 0,
    val acquisitionDate: LocalDate? = null,
    @field:PositiveOrZero
    val price: BigDecimal? = null,
    val imageUrl: String? = null
)

data class ProductUpdate(
    val categoryId: String? = null,
    val storeId: String? = null,
    val name: String? = null,
    val brand: String? = null,
    @field:Min(0)
    val quantity: Int? = null,
    @field:Min(0)
    val minStock: Int? = null,
    val acquisitionDate: LocalDate? = null,
    @field:PositiveOrZero
    val price: BigDecimal? = null,
    val imageUrl: String? = null
)

data class ProductResult(
    val id: String,
    val userId: String,
    val categoryId: String?,
    val storeId: String?,
    val name: String,
    val brand: String?,
    val quantity: Int,
    val minStock: Int,
    val acquisitionDate: LocalDate?,
    val price: BigDecimal?,
    val imageUrl: String?
)

/* =========================
   MOVEMENT
   ========================= */

data class MovementInput(
    @field:NotBlank
    val productId: String,
    @field:NotNull
    val type: MovementType,
    @field:NotNull
    val quantity: Int,
    @field:Positive
    val unitPrice: BigDecimal? = null, // requerido si PURCHASE
    val occurredAt: OffsetDateTime? = null,
    @field:Size(max = 280)
    val note: String? = null
)

data class MovementUpdate(
    val type: MovementType? = null,
    val quantity: Int? = null,
    @field:Positive
    val unitPrice: BigDecimal? = null,
    val occurredAt: OffsetDateTime? = null,
    @field:Size(max = 280)
    val note: String? = null
)

data class MovementResult(
    val id: String,
    val productId: String,
    val type: MovementType,
    val quantity: Int,
    val unitPrice: BigDecimal?,
    val occurredAt: OffsetDateTime,
    val note: String?
)
