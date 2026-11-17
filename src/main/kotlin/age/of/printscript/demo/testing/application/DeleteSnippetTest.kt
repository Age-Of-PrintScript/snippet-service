package age.of.printscript.demo.testing.application

import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.application.port.PermissionGateway
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.application.port.SnippetTestStorage
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestRepository

class DeleteSnippetTest(
    private val snippetRepository: SnippetRepository,
    private val snippetTestRepository: SnippetTestRepository,
    private val snippetTestStorage: SnippetTestStorage,
    private val permissionGateway: PermissionGateway? = null,
) : UseCase {
    @Suppress("ReturnCount")
    operator fun invoke(request: DeleteSnippetTestDto): DeleteSnippetTestResult {
        val test =
            snippetTestRepository.findById(request.testId)
                ?: return DeleteSnippetTestResult.NotFound

        val snippet =
            snippetRepository.findLatest(test.snippetId)
                ?: return DeleteSnippetTestResult.NotFound

        val hasAccess =
            snippet.ownerId == request.requesterId ||
                permissionGateway?.hasWriteAccess(snippet.id, request.requesterId) == true
        if (!hasAccess) {
            return DeleteSnippetTestResult.Forbidden
        }

        snippetTestStorage.delete(test.contentUrl)
        snippetTestRepository.deleteById(test.id)

        return DeleteSnippetTestResult.Success
    }
}

data class DeleteSnippetTestDto(
    val testId: SnippetTestId,
    val requesterId: UserId,
)

sealed interface DeleteSnippetTestResult {
    data object Success : DeleteSnippetTestResult

    data object NotFound : DeleteSnippetTestResult

    data object Forbidden : DeleteSnippetTestResult
}
