package age.of.printscript.demo.lint.application

import age.of.printscript.demo.lint.domain.LintStatus
import age.of.printscript.demo.lint.domain.SnippetLintStatus
import age.of.printscript.demo.lint.domain.SnippetLintStatusRepository
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId

class InMemorySnippetLintStatusRepository(
    initialStatuses: List<SnippetLintStatus> = emptyList(),
) : SnippetLintStatusRepository {
    private val statuses =
        initialStatuses
            .associateBy { it.snippetId to it.version }
            .toMutableMap()

    override fun findByVersion(
        snippetId: SnippetId,
        version: Int,
    ): SnippetLintStatus? = statuses[snippetId to version]

    override fun save(lintStatus: SnippetLintStatus): SnippetLintStatus {
        statuses[lintStatus.snippetId to lintStatus.version] = lintStatus
        return lintStatus
    }

    override fun markAllPending(snippets: List<Snippet>) {
        snippets.forEach { snippet ->
            statuses[snippet.id to snippet.version] =
                SnippetLintStatus(
                    snippetId = snippet.id,
                    version = snippet.version,
                    status = LintStatus.PENDING,
                )
        }
    }
}
