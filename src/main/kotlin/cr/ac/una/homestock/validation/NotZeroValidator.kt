@file:Suppress("unused")
package cr.ac.una.homestock.validation

import cr.ac.una.homestock.dto.NotZero
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class NotZeroValidator : ConstraintValidator<NotZero, Int?> {
    override fun isValid(value: Int?, context: ConstraintValidatorContext?): Boolean {
        return value == null || value != 0
    }
}

