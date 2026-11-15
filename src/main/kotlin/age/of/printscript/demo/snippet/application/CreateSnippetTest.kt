package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.application.port.SnippetTestRepository
import age.of.printscript.demo.snippet.application.port.SnippetTestStorage
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.SnippetTest
import age.of.printscript.demo.snippet.domain.SnippetTestId
import age.of.printscript.demo.snippet.domain.SnippetTestSummary
import age.of.printscript.demo.snippet.domain.UserId
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
        val test =
            SnippetTest(
                id = SnippetTestId(UUID.randomUUID().toString()),
                snippetId = snippet.id,
                ownerId = request.ownerId,
                inputs = request.inputs,
                outputs = request.outputs,
            )
        val contentUrl = snippetTestStorage.save(test)

        return try {
            CreateSnippetTestResult.Created(snippetTestRepository.save(test, contentUrl))
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
