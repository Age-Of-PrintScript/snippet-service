package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.lint.domain.LintStatus
import age.of.printscript.demo.lint.domain.SnippetLintStatusRepository
import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.application.port.PermissionGateway
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId

enum class SnippetRelation {
    ALL,
    OWNED,
    SHARED,
}

enum class SnippetSortField {
    NAME,
    LANGUAGE,
    COMPLIANCE,
}

enum class SortDirection {
    ASC,
    DESC,
}

class ListSnippets(
    private val permissionGateway: PermissionGateway,
    private val snippetRepository: SnippetRepository,
    private val lintStatusRepository: SnippetLintStatusRepository,
) : UseCase {
    @Suppress("LongParameterList")
    operator fun invoke(
        page: Int = 0,
        pageSize: Int = 10,
        name: String? = null,
        relation: SnippetRelation = SnippetRelation.ALL,
        language: String? = null,
        compliance: LintStatus? = null,
        sortBy: SnippetSortField? = null,
        sortDir: SortDirection = SortDirection.ASC,
    ): ListSnippetsResult {
        val safePage = page.coerceAtLeast(0)
        val safeSize = pageSize.coerceAtLeast(1)
        val accessible = filterByRelation(permissionGateway.listAccessibleSnippets(), relation)
        if (accessible.isEmpty()) {
            return ListSnippetsResult(page = safePage, pageSize = safeSize, total = 0, snippets = emptyList())
        }

        val normalizedLanguage = language?.trim()?.takeIf { it.isNotEmpty() }
        val inMemory = normalizedLanguage != null || compliance != null || sortBy != null
        val snippetPage =
            snippetRepository.findLatestByIds(
                ids = accessible.map { it.snippetId },
                name = name,
                page = if (inMemory) 0 else safePage,
                pageSize = if (inMemory) Int.MAX_VALUE else safeSize,
            )
        val summaries = toSummaries(snippetPage.items, accessible)

        return if (!inMemory) {
            ListSnippetsResult(page = safePage, pageSize = safeSize, total = snippetPage.total, snippets = summaries)
        } else {
            paginateFiltered(summaries, normalizedLanguage, compliance, sortBy, sortDir, safePage, safeSize)
        }
    }

    private fun filterByRelation(
        accessible: List<age.of.printscript.demo.snippet.application.port.AccessibleSnippet>,
        relation: SnippetRelation,
    ) = accessible.filter { item ->
        when (relation) {
            SnippetRelation.ALL -> true
            SnippetRelation.OWNED -> item.role.equals(OWNER_ROLE, ignoreCase = true)
            SnippetRelation.SHARED -> !item.role.equals(OWNER_ROLE, ignoreCase = true)
        }
    }

    private fun toSummaries(
        snippets: List<age.of.printscript.demo.snippet.domain.Snippet>,
        accessible: List<age.of.printscript.demo.snippet.application.port.AccessibleSnippet>,
    ): List<SnippetSummary> {
        val rolesById = accessible.associate { it.snippetId to it.role }
        val statuses = lintStatusRepository.findStatuses(snippets)
        return snippets.map { snippet ->
            SnippetSummary(
                snippetId = snippet.id,
                title = snippet.title,
                compliance = statuses[snippet.id] ?: LintStatus.PENDING,
                author = snippet.ownerId,
                language = snippet.language,
                languageVersion = snippet.languageVersion,
                role = rolesById[snippet.id] ?: DEFAULT_ROLE,
            )
        }
    }

    @Suppress("LongParameterList")
    private fun paginateFiltered(
        summaries: List<SnippetSummary>,
        language: String?,
        compliance: LintStatus?,
        sortBy: SnippetSortField?,
        sortDir: SortDirection,
        page: Int,
        pageSize: Int,
    ): ListSnippetsResult {
        val filtered =
            summaries.filter { summary ->
                (language == null || summary.language.equals(language, ignoreCase = true)) &&
                    (compliance == null || summary.compliance == compliance)
            }
        val sorted = sortSummaries(filtered, sortBy, sortDir)
        val fromIndex = (page * pageSize).coerceAtMost(sorted.size)
        val toIndex = (fromIndex + pageSize).coerceAtMost(sorted.size)
        return ListSnippetsResult(
            page = page,
            pageSize = pageSize,
            total = sorted.size.toLong(),
            snippets = sorted.subList(fromIndex, toIndex),
        )
    }

    private fun sortSummaries(
        summaries: List<SnippetSummary>,
        sortBy: SnippetSortField?,
        sortDir: SortDirection,
    ): List<SnippetSummary> {
        val comparator =
            when (sortBy) {
                SnippetSortField.NAME -> compareBy<SnippetSummary> { it.title.lowercase() }
                SnippetSortField.LANGUAGE -> compareBy<SnippetSummary> { it.language.lowercase() }
                SnippetSortField.COMPLIANCE -> compareBy<SnippetSummary> { it.compliance.name }
                null -> return summaries
            }
        val directed = if (sortDir == SortDirection.DESC) comparator.reversed() else comparator
        return summaries.sortedWith(directed)
    }

    private companion object {
        const val OWNER_ROLE = "OWNER"
        const val DEFAULT_ROLE = "VIEWER"
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
