package age.of.printscript.demo.lint.domain

import age.of.printscript.demo.snippet.domain.SnippetId

interface SnippetLintStatusRepository {
    fun findByVersion(
        snippetId: SnippetId,
        version: Int,
    ): SnippetLintStatus?

    fun save(lintStatus: SnippetLintStatus): SnippetLintStatus
}
