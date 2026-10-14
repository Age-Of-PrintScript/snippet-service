package age.of.printscript.demo.application

import age.of.printscript.demo.application.port.PermissionGateway
import age.of.printscript.demo.application.port.SnippetStorage
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.SnippetRepository
import age.of.printscript.demo.domain.SnippetWithContent
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.domain.withContent

class GetSnippet(
    private val snippetRepository: SnippetRepository,
    private val snippetStorage: SnippetStorage,
    private val permissionGateway: PermissionGateway,
) : UseCase {
    operator fun invoke(
        id: SnippetId,
        userId: UserId,
        version: Int? = null,
    ): GetSnippetResult {
        val snippet =
            if (version != null) {
                snippetRepository.findByVersion(id, version)
            } else {
                snippetRepository.findLatest(id)
            } ?: return GetSnippetResult.NotFound

        val hasAccess = snippet.ownerId == userId || permissionGateway.hasReadAccess(snippet.id, userId)
        return when {
            !hasAccess -> GetSnippetResult.Forbidden
            else -> {
                val content = snippetStorage.read(snippet.contentUrl)
                GetSnippetResult.Success(snippet.withContent(content))
            }
        }
    }
}

sealed interface GetSnippetResult {
    data class Success(
        val snippet: SnippetWithContent,
    ) : GetSnippetResult

    data object NotFound : GetSnippetResult

    data object Forbidden : GetSnippetResult
}
