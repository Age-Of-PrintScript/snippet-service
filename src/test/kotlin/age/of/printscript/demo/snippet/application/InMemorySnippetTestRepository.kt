package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.snippet.application.port.SnippetTestRepository
import age.of.printscript.demo.snippet.domain.SnippetTest
import age.of.printscript.demo.snippet.domain.SnippetTestSummary
import java.time.Instant

class InMemorySnippetTestRepository(
    private val saveFailure: SnippetTestPersistenceException? = null,
) : SnippetTestRepository {
    val savedTests = mutableListOf<Pair<SnippetTest, String>>()

    override fun save(
        test: SnippetTest,
        contentUrl: String,
    ): SnippetTestSummary {
        saveFailure?.let { throw it }
        savedTests += test to contentUrl
        return SnippetTestSummary(
            id = test.id,
            snippetId = test.snippetId,
            ownerId = test.ownerId,
            createdAt = Instant.now(),
        )
    }
}
