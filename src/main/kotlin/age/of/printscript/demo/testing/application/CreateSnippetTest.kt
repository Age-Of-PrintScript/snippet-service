package age.of.printscript.demo.testing.application

import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.application.port.SnippetTestStorage
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestContent
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestRepository
import age.of.printscript.demo.testing.domain.SnippetTestSummary
import java.util.UUID

class CreateSnippetTest(
    private val snippetRepository: SnippetRepository,
    private val snippetTestRepository: SnippetTestRepository,
    private val snippetTestStorage: SnippetTestStorage,
) : UseCase {
    operator fun invoke(request: CreateSnippetTestDto): CreateSnippetTestResult {
        val snippet = snippetRepository.findLatest(request.snippetId)

        return when {
            snippet == null -> CreateSnippetTestResult.NotFound
            snippet.ownerId != request.ownerId -> CreateSnippetTestResult.Forbidden
            else -> persistTest(snippet, request)
        }
    }

    private fun persistTest(
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
    val inputs: List<String>,
    val outputs: List<String>,
)

sealed interface CreateSnippetTestResult {
    data class Created(
        val test: SnippetTestSummary,
    ) : CreateSnippetTestResult

    data object NotFound : CreateSnippetTestResult

    data object Forbidden : CreateSnippetTestResult
}
