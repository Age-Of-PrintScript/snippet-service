package age.of.printscript.demo.testing.application

import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestRepository
import age.of.printscript.demo.testing.domain.SnippetTestStatus
import age.of.printscript.demo.testing.domain.SnippetTestSummary
import java.time.Instant

class InMemorySnippetTestRepository(
    private val saveFailure: SnippetTestPersistenceException? = null,
) : SnippetTestRepository {
    val savedTests = mutableListOf<Pair<SnippetTest, String>>()

    override fun findAllBySnippetId(snippetId: SnippetId): List<SnippetTestSummary> =
        savedTests.map { it.first }.filter { it.snippetId == snippetId }.map {
            SnippetTestSummary(it.id, it.snippetId, it.ownerId, it.name, Instant.now(), it.contentUrl, it.status)
        }

    override fun findById(id: SnippetTestId): SnippetTest? = savedTests.map { it.first }.find { it.id == id }

    override fun markPending(
        id: SnippetTestId,
        jobId: JobId,
    ) {
        val index = savedTests.indexOfFirst { it.first.id == id }
        check(index >= 0) { "Test ${id.value} no longer exists" }
        val test = savedTests[index].first.copy(status = SnippetTestStatus.PENDING, activeJobId = jobId)
        savedTests[index] = test to test.contentUrl
    }

    override fun completeIfActive(
        jobId: JobId,
        status: SnippetTestStatus,
    ) {
        savedTests.replaceAll { (test, url) ->
            if (test.activeJobId == jobId && test.status == SnippetTestStatus.PENDING) {
                test.copy(status = status, activeJobId = null) to url
            } else {
                test to url
            }
        }
    }

    override fun invalidateForSnippet(snippetId: SnippetId) {
        savedTests.replaceAll { (test, url) ->
            if (test.snippetId == snippetId) {
                test.copy(status = SnippetTestStatus.NOT_RUN, activeJobId = null) to url
            } else {
                test to url
            }
        }
    }

    override fun deleteById(id: SnippetTestId) {
        savedTests.removeIf { it.first.id == id }
    }

    override fun save(test: SnippetTest): SnippetTestSummary {
        saveFailure?.let { throw it }
        val index = savedTests.indexOfFirst { it.first.id == test.id }
        if (index >= 0) {
            savedTests[index] = test to test.contentUrl
        } else {
            savedTests += test to test.contentUrl
        }
        return SnippetTestSummary(
            id = test.id,
            snippetId = test.snippetId,
            ownerId = test.ownerId,
            name = test.name,
            createdAt = Instant.now(),
            contentUrl = test.contentUrl,
            status = test.status,
        )
    }

    override fun deleteAllBySnippetId(snippetId: SnippetId) {
        savedTests.removeIf { it.first.snippetId == snippetId }
    }
}
