package age.of.printscript.demo.lint.domain

import age.of.printscript.demo.snippet.domain.SnippetId

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
