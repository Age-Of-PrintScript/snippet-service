package age.of.printscript.demo.testing.application

import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.application.port.PermissionGateway
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.application.port.SnippetTestStorage
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestContent
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestRepository
import age.of.printscript.demo.testing.domain.SnippetTestStatus
import age.of.printscript.demo.testing.domain.SnippetTestSummary
import java.util.UUID

class CreateSnippetTest(
    private val snippetRepository: SnippetRepository,
    private val snippetTestRepository: SnippetTestRepository,
    private val snippetTestStorage: SnippetTestStorage,
    private val permissionGateway: PermissionGateway? = null,
) : UseCase {
    @Suppress("ReturnCount")
    operator fun invoke(request: CreateSnippetTestDto): CreateSnippetTestResult {
        val snippet =
            snippetRepository.findLatest(request.snippetId)
                ?: return CreateSnippetTestResult.NotFound

        val hasAccess =
            snippet.ownerId == request.ownerId ||
                permissionGateway?.hasWriteAccess(snippet.id, request.ownerId) == true
        if (!hasAccess) {
            return CreateSnippetTestResult.Forbidden
        }

        return if (request.id != null) {
            updateTest(snippet, request.id, request)
        } else {
            createTest(snippet, request)
        }
    }

    @Suppress("ReturnCount")
    private fun updateTest(
        snippet: Snippet,
        testId: SnippetTestId,
        request: CreateSnippetTestDto,
    ): CreateSnippetTestResult {
        val existing =
            snippetTestRepository.findById(testId)
                ?: return CreateSnippetTestResult.NotFound

        if (existing.snippetId != snippet.id) {
            return CreateSnippetTestResult.NotFound
        }

        val content = SnippetTestContent(request.inputs, request.outputs)
        val contentUrl = snippetTestStorage.save(testId, content)
        val updated =
            existing.copy(
                name = request.name,
                contentUrl = contentUrl,
                status = SnippetTestStatus.NOT_RUN,
                activeJobId = null,
            )

        return try {
            CreateSnippetTestResult.Created(snippetTestRepository.save(updated))
        } catch (failure: SnippetTestPersistenceException) {
            if (existing.contentUrl != contentUrl) {
                snippetTestStorage.delete(contentUrl)
            }
            throw failure
        }
    }

    private fun createTest(
        snippet: Snippet,
        request: CreateSnippetTestDto,
    ): CreateSnippetTestResult {
        val testId = SnippetTestId(UUID.randomUUID().toString())
        val content = SnippetTestContent(request.inputs, request.outputs)
        val contentUrl = snippetTestStorage.save(testId, content)
        val test =
            SnippetTest(
                id = testId,
                snippetId = snippet.id,
                ownerId = request.ownerId,
                name = request.name,
                contentUrl = contentUrl,
            )

        return try {
            CreateSnippetTestResult.Created(snippetTestRepository.save(test))
        } catch (failure: SnippetTestPersistenceException) {
            snippetTestStorage.delete(contentUrl)
            throw failure
        }
    }
}

data class CreateSnippetTestDto(
    val snippetId: SnippetId,
    val ownerId: UserId,
    val name: String,
    val inputs: List<String>,
    val outputs: List<String>,
    val id: SnippetTestId? = null,
)

sealed interface CreateSnippetTestResult {
    data class Created(
        val test: SnippetTestSummary,
    ) : CreateSnippetTestResult

    data object NotFound : CreateSnippetTestResult

    data object Forbidden : CreateSnippetTestResult
}
