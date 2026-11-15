package age.of.printscript.demo.testing.application

import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestRepository
import age.of.printscript.demo.testing.domain.SnippetTestSummary
import java.time.Instant

class InMemorySnippetTestRepository(
    private val saveFailure: SnippetTestPersistenceException? = null,
) : SnippetTestRepository {
    val savedTests = mutableListOf<Pair<SnippetTest, String>>()

    override fun save(test: SnippetTest): SnippetTestSummary {
        saveFailure?.let { throw it }
        savedTests += test to test.contentUrl
        return SnippetTestSummary(
            id = test.id,
            snippetId = test.snippetId,
            ownerId = test.ownerId,
            createdAt = Instant.now(),
        )
    }
}
