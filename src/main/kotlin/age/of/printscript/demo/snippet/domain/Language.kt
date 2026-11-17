package age.of.printscript.demo.snippet.domain

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

data class LanguageDescriptor(
    val name: String,
    val fileTypes: List<String>,
    val versions: List<String>,
)

interface Language {
    val name: String
    val fileTypes: List<String>
    val versions: List<String>

    fun validate(
        code: String,
        version: String,
    ): ValidationResult
}
