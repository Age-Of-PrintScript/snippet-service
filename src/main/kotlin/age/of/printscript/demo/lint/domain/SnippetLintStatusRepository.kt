package age.of.printscript.demo.lint.domain

import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId

interface SnippetLintStatusRepository {
    fun findByVersion(
        snippetId: SnippetId,
        version: Int,
    ): SnippetLintStatus?

    fun findStatuses(snippets: List<Snippet>): Map<SnippetId, LintStatus>

    fun save(lintStatus: SnippetLintStatus): SnippetLintStatus

    fun markAllPending(snippets: List<Snippet>)
}
