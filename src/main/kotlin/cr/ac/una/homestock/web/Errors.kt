package cr.ac.una.homestock.web


import cr.ac.una.homestock.service.NotFoundException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import jakarta.validation.ConstraintViolationException

data class ApiError(val status: Int, val error: String, val message: String?)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun notFound(ex: NotFoundException) =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError(404, "Not Found", ex.message))

    // Body inválido (@Valid @RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun bodyValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val msg = ex.bindingResult.fieldErrors.joinToString { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiError(422, "Validation Error", msg))
    }

    // Binding a objetos (e.g., @ModelAttribute) o errores de field genéricos
    @ExceptionHandler(BindException::class)
    fun bindValidation(ex: BindException): ResponseEntity<ApiError> {
        val msg = ex.bindingResult.fieldErrors.joinToString { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiError(422, "Validation Error", msg))
    }

    // Validación por constraints (e.g., @Min, @Pattern) en path/query
    @ExceptionHandler(ConstraintViolationException::class)
    fun constraintValidation(ex: ConstraintViolationException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiError(422, "Validation Error", ex.message))

    // JSON mal formado u tipos inválidos en body
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun unreadable(ex: HttpMessageNotReadableException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(400, "Bad Request", ex.mostSpecificCause?.message ?: ex.message))

    // Conflictos de unicidad (duplicados)
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun conflict(ex: DataIntegrityViolationException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiError(409, "Conflict", ex.rootCause?.message ?: ex.message))

    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(ex: IllegalArgumentException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(400, "Bad Request", ex.message))

    @ExceptionHandler(Exception::class)
    fun generic(ex: Exception) =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError(500, "Internal Server Error", ex.message))
}
