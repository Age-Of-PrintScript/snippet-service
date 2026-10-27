package age.of.printscript.demo.application

import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.SnippetLintStatus
import age.of.printscript.demo.domain.SnippetLintStatusRepository

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
}
