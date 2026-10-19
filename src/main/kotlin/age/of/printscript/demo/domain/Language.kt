package age.of.printscript.demo.domain

data class ValidationError(
    val message: String,
)

sealed interface ValidationResult {
    data object Success : ValidationResult

    data class Failure(
        val errors: List<ValidationError>,
    ) : ValidationResult

    data object UnsupportedVersion : ValidationResult
}

interface Language {
    val name: String

    fun validate(
        code: String,
        version: String,
    ): ValidationResult
}
