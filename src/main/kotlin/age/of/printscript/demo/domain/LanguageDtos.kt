package age.of.printscript.demo.domain

sealed interface ValidationResult {
    data object Valid : ValidationResult

    data class Invalid(
        val rule: String,
        val line: Int,
        val column: Int,
    ) : ValidationResult
}

data class LintRules(
    val rules: List<String> = emptyList(),
)

data class LintResult(
    val warnings: List<String>,
)

data class FormatRules(
    val rules: List<String> = emptyList(),
)

data class ExecutionResult(
    val outputs: List<String> = emptyList(),
)
