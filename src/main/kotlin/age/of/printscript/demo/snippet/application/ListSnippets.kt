package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.lint.domain.LintStatus
import age.of.printscript.demo.lint.domain.SnippetLintStatusRepository
import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.application.port.PermissionGateway
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId

class ListSnippets(
    private val permissionGateway: PermissionGateway,
    private val snippetRepository: SnippetRepository,
    private val lintStatusRepository: SnippetLintStatusRepository,
) : UseCase {
    operator fun invoke(
        page: Int = 0,
        pageSize: Int = 10,
        name: String? = null,
    ): ListSnippetsResult {
        val safePage = page.coerceAtLeast(0)
        val safeSize = pageSize.coerceAtLeast(1)

        val accessible = permissionGateway.listAccessibleSnippets()
        if (accessible.isEmpty()) {
            return ListSnippetsResult(page = safePage, pageSize = safeSize, total = 0, snippets = emptyList())
        }
        /** Lo que llega de permissionGateway.listAccessibleSnippets()
         [
         {snippetId: , rol: },
         {snippetId: , rol: },

         ]
         */
        val rolesById = accessible.associate { it.snippetId to it.role }
        val snippetPage =
            snippetRepository.findLatestByIds(
                ids = accessible.map { it.snippetId },
                name = name,
                page = safePage,
                pageSize = safeSize,
            )
        val statuses = lintStatusRepository.findStatuses(snippetPage.items)

        val summaries =
            snippetPage.items.map { snippet ->
                SnippetSummary(
                    snippetId = snippet.id,
                    title = snippet.title,
                    compliance = statuses[snippet.id] ?: LintStatus.PENDING,
                    author = snippet.ownerId,
                    language = snippet.language,
                    languageVersion = snippet.languageVersion,
                    role = rolesById[snippet.id] ?: "VIEWER",
                )
            }

        return ListSnippetsResult(
            page = safePage,
            pageSize = safeSize,
            total = snippetPage.total,
            snippets = summaries,
        )
    }
}

data class SnippetSummary(
    val snippetId: SnippetId,
    val title: String,
    val compliance: LintStatus,
    val author: UserId,
    val language: String,
    val languageVersion: String,
    val role: String,
)

data class ListSnippetsResult(
    val page: Int,
    val pageSize: Int,
    val total: Long,
    val snippets: List<SnippetSummary>,
)
