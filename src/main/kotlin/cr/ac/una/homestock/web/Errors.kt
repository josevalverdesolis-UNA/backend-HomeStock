package cr.una.homestock.web.error

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.ServletWebRequest
import java.time.OffsetDateTime

data class ApiError(
    val timestamp: OffsetDateTime = OffsetDateTime.now(),
    val status: Int,
    val error: String,
    val message: String?,
    val path: String
)

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException, req: ServletWebRequest) =
        build(HttpStatus.NOT_FOUND, "ELEMENT_NOT_FOUND", ex.message, req)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException, req: ServletWebRequest): ResponseEntity<ApiError> {
        val msg = ex.bindingResult.fieldErrors.joinToString { "${it.field}: ${it.defaultMessage}" }
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", msg, req)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleIntegrity(ex: DataIntegrityViolationException, req: ServletWebRequest) =
        build(HttpStatus.CONFLICT, "DATA_INTEGRITY", ex.mostSpecificCause.message, req)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArg(ex: IllegalArgumentException, req: ServletWebRequest) =
        build(HttpStatus.BAD_REQUEST, "BUSINESS_RULE", ex.message, req)

    @ExceptionHandler(Exception::class)
    fun handleAll(ex: Exception, req: ServletWebRequest) =
        build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.message, req)

    private fun build(status: HttpStatus, error: String, msg: String?, req: ServletWebRequest)
            : ResponseEntity<ApiError> =
        ResponseEntity
            .status(status)
            .body(ApiError(status = status.value(), error = error, message = msg, path = req.request.requestURI))
}
