package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.application.port.SnippetStorage
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId
import org.slf4j.LoggerFactory

class DeleteSnippet(
    private val snippetRepository: SnippetRepository,
    private val snippetStorage: SnippetStorage,
) : UseCase {
    private val logger = LoggerFactory.getLogger(javaClass)

    operator fun invoke(
        id: SnippetId,
        userId: UserId,
    ): DeleteSnippetResult {
        val versions = snippetRepository.findAllVersions(id)
        val current = versions.firstOrNull { it.isLatest } ?: versions.firstOrNull()
        return when {
            current == null -> DeleteSnippetResult.NotFound
            current.ownerId != userId -> DeleteSnippetResult.Forbidden
            else -> performDelete(id, versions)
        }
    }

    private fun performDelete(
        id: SnippetId,
        versions: List<Snippet>,
    ): DeleteSnippetResult {
        snippetRepository.deleteById(id)
        // Best-effort en storage: si algún blob ya no existe, la DB ya quedó limpia.
        versions.forEach { version ->
            runCatching { snippetStorage.delete(version.contentUrl) }
                .onFailure { logger.warn("Failed to delete blob {} for snippet {}", version.contentUrl, id.value, it) }
        }
        return DeleteSnippetResult.Deleted
    }
}

sealed interface DeleteSnippetResult {
    data object Deleted : DeleteSnippetResult

    data object NotFound : DeleteSnippetResult

    data object Forbidden : DeleteSnippetResult
}
