package age.of.printscript.demo.domain

data class ValidationError(
    val rule: String,
    val line: Int,
    val column: Int,
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
        url: String,
        version: String,
    ): ValidationResult
}
