package age.of.printscript.demo.application

import age.of.printscript.demo.application.port.PermissionGateway
import age.of.printscript.demo.domain.Snippet
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.SnippetRepository
import age.of.printscript.demo.domain.UserId

class GetSnippet(
    private val snippetRepository: SnippetRepository,
    private val permissionGateway: PermissionGateway,
) : UseCase {

    operator fun invoke(id: SnippetId, userId: UserId, version: Int? = null): GetSnippetResult {
        val snippet = if (version != null) {
            snippetRepository.findByVersion(id, version)
        } else {
            snippetRepository.findLatest(id)
        } ?: return GetSnippetResult.NotFound

        val hasAccess = permissionGateway.hasReadAccess(snippet.id, userId)
        if (!hasAccess) {
            return GetSnippetResult.Forbidden
        }

        return GetSnippetResult.Success(snippet)
    }
}

sealed interface GetSnippetResult {
    data class Success(val snippet: Snippet) : GetSnippetResult
    data object NotFound : GetSnippetResult
    data object Forbidden : GetSnippetResult
}
