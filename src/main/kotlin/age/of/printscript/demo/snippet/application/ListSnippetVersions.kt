package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.application.port.PermissionGateway
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId

class ListSnippetVersions(
    private val snippetRepository: SnippetRepository,
    private val permissionGateway: PermissionGateway,
) : UseCase {
    operator fun invoke(
        id: SnippetId,
        userId: UserId,
    ): ListSnippetVersionsResult {
        val latest = snippetRepository.findLatest(id) ?: return ListSnippetVersionsResult.NotFound
        val hasAccess = latest.ownerId == userId || permissionGateway.hasReadAccess(latest.id, userId)
        return if (!hasAccess) {
            ListSnippetVersionsResult.Forbidden
        } else {
            ListSnippetVersionsResult.Success(snippetRepository.findAllVersions(id))
        }
    }
}

sealed interface ListSnippetVersionsResult {
    data class Success(
        val versions: List<Snippet>,
    ) : ListSnippetVersionsResult

    data object NotFound : ListSnippetVersionsResult

    data object Forbidden : ListSnippetVersionsResult
}
