package age.of.printscript.demo.domain

enum class LintStatus {
    PENDING,
    COMPLIANT,
    NON_COMPLIANT,
    FAILED,
}

data class LintWarning(
    val message: String,
    val line: Int,
    val column: Int,
)

data class SnippetLintStatus(
    val snippetId: SnippetId,
    val version: Int,
    val status: LintStatus,
    val warnings: List<LintWarning> = emptyList(),
)
