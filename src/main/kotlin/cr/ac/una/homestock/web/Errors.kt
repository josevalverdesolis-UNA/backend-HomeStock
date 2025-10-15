package cr.ac.una.homestock.web

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.OffsetDateTime

sealed class ApiSubError
data class ApiValidationError(val field: String, val message: String?) : ApiSubError()

data class ApiError(
    val status: Int,
    val code: String,
    val message: String?,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val timestamp: OffsetDateTime = OffsetDateTime.now(),
    val errors: List<ApiSubError>? = null,
)

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    fun notFound(ex: NoSuchElementException) =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiError(HttpStatus.NOT_FOUND.value(), "ELEMENT_NOT_FOUND", ex.message)
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val details = ex.bindingResult.allErrors.mapNotNull {
            if (it is FieldError) ApiValidationError(it.field, it.defaultMessage) else null
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
            ApiError(HttpStatus.UNPROCESSABLE_ENTITY.value(), "VALIDATION_ERROR", "Validation failed", errors = details)
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(ex: IllegalArgumentException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiError(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", ex.message)
        )

    @ExceptionHandler(Exception::class)
    fun generic(ex: Exception) =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR", ex.message)
        )
}
