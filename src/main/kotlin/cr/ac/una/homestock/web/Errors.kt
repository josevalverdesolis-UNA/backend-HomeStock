package cr.ac.una.homestock.web.error

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.OffsetDateTime

/* =========================
   EXCEPCIONES
   ========================= */

class NotFoundException(message: String) : RuntimeException(message)
class BusinessException(message: String) : RuntimeException(message)

/* =========================
   MODELO DE ERROR
   ========================= */

data class ApiError(
    val status: Int,
    val error: String,
    val message: String?,
    val timestamp: OffsetDateTime = OffsetDateTime.now(),
    val fieldErrors: List<FieldViolation>? = null
)

data class FieldViolation(
    val field: String,
    val message: String?
)

/* =========================
   HANDLER GLOBAL
   ========================= */

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiError(404, "Not Found", ex.message)
        )

    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(ex: BusinessException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
            ApiError(422, "Unprocessable Entity", ex.message)
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val fields = ex.bindingResult.allErrors.mapNotNull {
            val field = (it as? FieldError)?.field ?: return@mapNotNull null
            FieldViolation(field, it.defaultMessage)
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
            ApiError(422, "Validation Error", "Campos inválidos", fieldErrors = fields)
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraint(ex: ConstraintViolationException): ResponseEntity<ApiError> {
        val fields = ex.constraintViolations.map {
            FieldViolation(it.propertyPath.toString(), it.message)
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
            ApiError(422, "Validation Error", "Parámetros inválidos", fieldErrors = fields)
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiError(500, "Internal Server Error", ex.message)
        )
}
