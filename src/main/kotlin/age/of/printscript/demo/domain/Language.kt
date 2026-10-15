package age.of.printscript.demo.domain

sealed interface ValidationResult {
    data object Success : ValidationResult

    data class Failure(
        val errors: List<String>,
    ) : ValidationResult
}

interface Language {
    fun validate(
        url: String,
        version: String,
    ): ValidationResult
}
