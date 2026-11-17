package age.of.printscript.demo.testing.application

import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.application.port.PermissionGateway
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.application.port.SnippetTestStorage
import age.of.printscript.demo.testing.domain.SnippetTestContent
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestRepository
import age.of.printscript.demo.testing.domain.SnippetTestStatus
import java.time.Instant

data class VisibleTest(
    val id: SnippetTestId,
    val snippetId: SnippetId,
    val name: String,
    val createdAt: Instant,
    val content: SnippetTestContent,
    val status: SnippetTestStatus,
)

sealed interface ListSnippetTestsResult {
    data class Success(
        val tests: List<VisibleTest>,
    ) : ListSnippetTestsResult

    data object NotFound : ListSnippetTestsResult

    data object Forbidden : ListSnippetTestsResult
}

class ListSnippetTests(
    private val snippets: SnippetRepository,
    private val tests: SnippetTestRepository,
    private val storage: SnippetTestStorage,
    private val permissions: PermissionGateway,
) : UseCase {
    @Suppress("ReturnCount")
    operator fun invoke(
        snippetId: SnippetId,
        userId: UserId,
    ): ListSnippetTestsResult {
        val snippet = snippets.findLatest(snippetId) ?: return ListSnippetTestsResult.NotFound
        if (snippet.ownerId != userId && !permissions.hasReadAccess(snippetId, userId)) {
            return ListSnippetTestsResult.Forbidden
        }
        return ListSnippetTestsResult.Success(
            tests.findAllBySnippetId(snippetId).map { test ->
                VisibleTest(
                    id = test.id,
                    snippetId = test.snippetId,
                    name = test.name,
                    createdAt = test.createdAt,
                    content = storage.read(test.contentUrl),
                    status = test.status,
                )
            },
        )
    }
}
