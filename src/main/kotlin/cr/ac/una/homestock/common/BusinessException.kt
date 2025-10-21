package cr.ac.una.homestock.common

import org.springframework.http.HttpStatus

// Excepción de negocio desacoplada de la capa web para evitar dependencias cruzadas.
open class BusinessException(
    override val message: String,
    val httpStatus: HttpStatus = HttpStatus.UNPROCESSABLE_ENTITY
) : RuntimeException(message)

