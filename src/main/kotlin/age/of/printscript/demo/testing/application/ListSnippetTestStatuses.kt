package age.of.printscript.demo.testing.application

import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.application.port.PermissionGateway
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestRepository
import age.of.printscript.demo.testing.domain.SnippetTestStatus

data class TestStatusItem(
    val id: SnippetTestId,
    val status: SnippetTestStatus,
)

sealed interface ListSnippetTestStatusesResult {
    data class Success(
        val tests: List<TestStatusItem>,
    ) : ListSnippetTestStatusesResult

    data object NotFound : ListSnippetTestStatusesResult

    data object Forbidden : ListSnippetTestStatusesResult
}

class ListSnippetTestStatuses(
    private val snippets: SnippetRepository,
    private val tests: SnippetTestRepository,
    private val permissions: PermissionGateway,
) : UseCase {
    @Suppress("ReturnCount")
    operator fun invoke(
        snippetId: SnippetId,
        userId: UserId,
    ): ListSnippetTestStatusesResult {
        val snippet = snippets.findLatest(snippetId) ?: return ListSnippetTestStatusesResult.NotFound
        if (snippet.ownerId != userId && !permissions.hasReadAccess(snippetId, userId)) {
            return ListSnippetTestStatusesResult.Forbidden
        }
        return ListSnippetTestStatusesResult.Success(
            tests.findAllBySnippetId(snippetId).map { TestStatusItem(it.id, it.status) },
        )
    }
}
