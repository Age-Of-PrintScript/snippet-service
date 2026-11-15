package age.of.printscript.demo.testing.application

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestRepository
import age.of.printscript.demo.testing.domain.SnippetTestSummary
import java.time.Instant

class InMemorySnippetTestRepository(
    private val saveFailure: SnippetTestPersistenceException? = null,
) : SnippetTestRepository {
    val savedTests = mutableListOf<Pair<SnippetTest, String>>()

    override fun findAllBySnippetId(snippetId: SnippetId): List<SnippetTestSummary> =
        savedTests.map { it.first }.filter { it.snippetId == snippetId }.map {
            SnippetTestSummary(it.id, it.snippetId, it.ownerId, Instant.now(), it.contentUrl)
        }

    override fun findById(id: SnippetTestId): SnippetTest? = savedTests.map { it.first }.find { it.id == id }

    override fun save(test: SnippetTest): SnippetTestSummary {
        saveFailure?.let { throw it }
        savedTests += test to test.contentUrl
        return SnippetTestSummary(
            id = test.id,
            snippetId = test.snippetId,
            ownerId = test.ownerId,
            createdAt = Instant.now(),
            contentUrl = test.contentUrl,
        )
    }
}
