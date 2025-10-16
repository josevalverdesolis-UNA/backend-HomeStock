package cr.ac.una.homestock.web

import cr.ac.una.homestock.dto.NotZero
import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

/**
 * Manejo centralizado de errores para la capa web.
 * - Estructura consistente de respuesta (ApiError).
 * - Handlers específicos para validación y reglas de negocio.
 */

// ------------------------------
// Modelo de error API
// ------------------------------

data class FieldErrorItem(
    val field: String,
    val message: String
)

data class ApiError(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val error: String,
    val message: String?,
    val path: String,
    val errors: List<FieldErrorItem>? = null,
)

// ------------------------------
// Excepción de negocio
// ------------------------------

open class BusinessException(
    override val message: String,
    val httpStatus: HttpStatus = HttpStatus.UNPROCESSABLE_ENTITY
) : RuntimeException(message)

// ------------------------------
// Controller Advice global
// ------------------------------

@RestControllerAdvice
class GlobalExceptionHandler {

    // 1) Bean Validation: @Valid en cuerpos (DTOs)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val fieldErrors = ex.bindingResult.allErrors
            .mapNotNull {
                if (it is FieldError) FieldErrorItem(it.field, it.defaultMessage ?: "Invalid value") else null
            }
        val body = ApiError(
            status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            error = HttpStatus.UNPROCESSABLE_ENTITY.reasonPhrase,
            message = "Validation failed",
            path = req.requestURI,
            errors = if (fieldErrors.isEmpty()) null else fieldErrors
        )
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body)
    }

    // 2) Bean Validation: @Validated en params/path variables
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val violations = ex.constraintViolations.map { it.toFieldErrorItem() }
        val body = ApiError(
            status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            error = HttpStatus.UNPROCESSABLE_ENTITY.reasonPhrase,
            message = "Constraint violation",
            path = req.requestURI,
            errors = if (violations.isEmpty()) null else violations
        )
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body)
    }

    // 3) Request mal formado / body ilegible
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(ex: HttpMessageNotReadableException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val body = ApiError(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = ex.mostSpecificCause.message ?: ex.message,
            path = req.requestURI
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    // 4) Falta parámetro requerido
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(ex: MissingServletRequestParameterException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val body = ApiError(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = ex.message,
            path = req.requestURI
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    // 5) No encontrado (EntityNotFound / NoSuchElement)
    @ExceptionHandler(value = [EntityNotFoundException::class, NoSuchElementException::class])
    fun handleNotFound(ex: Exception, req: HttpServletRequest): ResponseEntity<ApiError> {
        val body = ApiError(
            status = HttpStatus.NOT_FOUND.value(),
            error = HttpStatus.NOT_FOUND.reasonPhrase,
            message = ex.message,
            path = req.requestURI
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
    }

    // 6) Integridad de datos (duplicados, FKs, etc.)
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(ex: DataIntegrityViolationException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val body = ApiError(
            status = HttpStatus.CONFLICT.value(),
            error = HttpStatus.CONFLICT.reasonPhrase,
            message = ex.rootCause?.message ?: ex.message,
            path = req.requestURI
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body)
    }

    // 7) Reglas de negocio expresas
    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(ex: BusinessException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val body = ApiError(
            status = ex.httpStatus.value(),
            error = ex.httpStatus.reasonPhrase,
            message = ex.message,
            path = req.requestURI
        )
        return ResponseEntity.status(ex.httpStatus).body(body)
    }

    // 8) Argumentos/estado inválidos
    @ExceptionHandler(value = [IllegalArgumentException::class, IllegalStateException::class])
    fun handleIllegal(ex: RuntimeException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val body = ApiError(
            status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            error = HttpStatus.UNPROCESSABLE_ENTITY.reasonPhrase,
            message = ex.message,
            path = req.requestURI
        )
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body)
    }

    // 9) Fallback 500
    @ExceptionHandler(Exception::class)
    fun handleUnknown(ex: Exception, req: HttpServletRequest): ResponseEntity<ApiError> {
        val body = ApiError(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            message = ex.message,
            path = req.requestURI
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }
}

// ------------------------------
// Soporte: mapeo de ConstraintViolation -> FieldErrorItem
// ------------------------------

private fun ConstraintViolation<*>.toFieldErrorItem(): FieldErrorItem {
    val field = propertyPath?.toString()?.substringAfter('.') ?: propertyPath.toString()
    return FieldErrorItem(field = field, message = message ?: "Invalid value")
}

// ------------------------------
// Implementación del validador para @NotZero (Int?)
// ------------------------------

class NotZeroValidator : ConstraintValidator<NotZero, Int?> {
    override fun isValid(value: Int?, context: ConstraintValidatorContext?): Boolean {
        return value == null || value != 0
    }
}

// Comentario de cambios: creado/actualizado archivo -> Errors.kt
