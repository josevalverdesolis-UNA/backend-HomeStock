@file:Suppress("unused")
package cr.ac.una.homestock.web

import cr.ac.una.homestock.common.BusinessException
import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.Instant

// Modelo de error API

data class FieldErrorItem(
    val field: String,
    val message: String
)

data class ApiError(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val code: String,
    val message: String?,
    val path: String,
    val errors: List<FieldErrorItem>? = null,
)

@RestControllerAdvice(basePackages = ["cr.ac.una.homestock"])
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val fieldErrors = ex.bindingResult.allErrors
            .mapNotNull { if (it is FieldError) FieldErrorItem(it.field, it.defaultMessage ?: "Invalid value") else null }
        val status = HttpStatus.BAD_REQUEST
        val body = ApiError(
            status = status.value(),
            code = status.name,
            message = "Validation failed",
            path = req.requestURI,
            errors = if (fieldErrors.isEmpty()) null else fieldErrors
        )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val violations = ex.constraintViolations.map { it.toFieldErrorItem() }
        val status = HttpStatus.BAD_REQUEST
        val body = ApiError(
            status = status.value(),
            code = status.name,
            message = "Constraint violation",
            path = req.requestURI,
            errors = if (violations.isEmpty()) null else violations
        )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(ex: HttpMessageNotReadableException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val status = HttpStatus.BAD_REQUEST
        val body = ApiError(
            status = status.value(),
            code = status.name,
            message = ex.mostSpecificCause.message ?: ex.message,
            path = req.requestURI
        )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(ex: MissingServletRequestParameterException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val status = HttpStatus.BAD_REQUEST
        val body = ApiError(
            status = status.value(),
            code = status.name,
            message = ex.message,
            path = req.requestURI
        )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(value = [EntityNotFoundException::class, NoSuchElementException::class])
    fun handleNotFound(ex: Exception, req: HttpServletRequest): ResponseEntity<ApiError> {
        val status = HttpStatus.NOT_FOUND
        val body = ApiError(
            status = status.value(),
            code = status.name,
            message = ex.message,
            path = req.requestURI
        )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(ex: NoResourceFoundException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val status = HttpStatus.NOT_FOUND
        val body = ApiError(
            status = status.value(),
            code = status.name,
            message = ex.message,
            path = req.requestURI
        )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(ex: DataIntegrityViolationException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val status = HttpStatus.CONFLICT
        val body = ApiError(
            status = status.value(),
            code = status.name,
            message = ex.rootCause?.message ?: ex.message,
            path = req.requestURI
        )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(ex: BusinessException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val status = ex.httpStatus
        val body = ApiError(
            status = status.value(),
            code = status.name,
            message = ex.message,
            path = req.requestURI
        )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val status = HttpStatus.UNAUTHORIZED
        val body = ApiError(
            status = status.value(),
            code = status.name,
            message = ex.message,
            path = req.requestURI
        )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val status = HttpStatus.UNPROCESSABLE_ENTITY
        val body = ApiError(
            status = status.value(),
            code = status.name,
            message = ex.message,
            path = req.requestURI
        )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnknown(ex: Exception, req: HttpServletRequest): ResponseEntity<ApiError> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        val body = ApiError(
            status = status.value(),
            code = status.name,
            message = ex.message,
            path = req.requestURI
        )
        return ResponseEntity.status(status).body(body)
    }
}

private fun ConstraintViolation<*>.toFieldErrorItem(): FieldErrorItem {
    val field = propertyPath?.toString()?.substringAfter('.') ?: propertyPath.toString()
    return FieldErrorItem(field = field, message = message ?: "Invalid value")
}
