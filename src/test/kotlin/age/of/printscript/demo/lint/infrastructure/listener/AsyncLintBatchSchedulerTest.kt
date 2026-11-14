package age.of.printscript.demo.lint.infrastructure.listener

import age.of.printscript.demo.jobs.application.FakeJobPublisher
import age.of.printscript.demo.jobs.application.InMemoryJobRepository
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.lint.application.InMemorySnippetLintStatusRepository
import age.of.printscript.demo.lint.application.LINT_JOB
import age.of.printscript.demo.lint.application.port.LintJobPublisher
import age.of.printscript.demo.lint.domain.LintStatus
import age.of.printscript.demo.lint.domain.SnippetLintStatus
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.net.ConnectException

class AsyncLintBatchSchedulerTest {
    @Test
    fun `scheduleAll saves PENDING job and publishes lint job for each snippet`() {
        val userId = UserId("alice")
        val snippet1 = createDummySnippet(id = SnippetId("s-1"), ownerId = userId, version = 1)
        val snippet2 = createDummySnippet(id = SnippetId("s-2"), ownerId = userId, version = 2)
        val rules = mapOf("identifier-format" to "camelCase")

        val jobPublisher = FakeJobPublisher()
        val jobRepo = InMemoryJobRepository()
        val lintStatusRepo = InMemorySnippetLintStatusRepository()
        val scheduler = AsyncLintBatchScheduler(jobPublisher, jobRepo, lintStatusRepo)

        scheduler.scheduleAll(userId, listOf(snippet1, snippet2), rules)

        assertEquals(2, jobPublisher.publishedLintJobs.size)

        val firstPublished = jobPublisher.publishedLintJobs[0]
        assertEquals(snippet1, firstPublished.snippet)
        assertEquals(rules, firstPublished.rules)
        val firstJob = jobRepo.findById(JobId(firstPublished.jobId))
        assertNotNull(firstJob)
        assertEquals(snippet1.id, firstJob?.snippetId)
        assertEquals(snippet1.version, firstJob?.snippetVersion)
        assertEquals(userId, firstJob?.userId)
        assertEquals(JobType(LINT_JOB), firstJob?.type)
        assertEquals(JobStatus.PENDING, firstJob?.status)

        val secondPublished = jobPublisher.publishedLintJobs[1]
        assertEquals(snippet2, secondPublished.snippet)
        assertEquals(rules, secondPublished.rules)
        val secondJob = jobRepo.findById(JobId(secondPublished.jobId))
        assertNotNull(secondJob)
        assertEquals(snippet2.id, secondJob?.snippetId)
    }

    @Test
    fun `scheduleAll marks snippet status as FAILED and continues when publishing fails for one snippet`() {
        val userId = UserId("alice")
        val failingSnippet = createDummySnippet(id = SnippetId("s-fail"), ownerId = userId, version = 1)
        val okSnippet = createDummySnippet(id = SnippetId("s-ok"), ownerId = userId, version = 1)
        val rules = mapOf("identifier-format" to "camelCase")

        val publishedSnippets = mutableListOf<Snippet>()
        val partiallyFailingPublisher =
            object : LintJobPublisher {
                override fun publishLintJob(
                    jobId: String,
                    snippet: Snippet,
                    rules: Map<String, String?>,
                ) {
                    if (snippet.id == failingSnippet.id) {
                        throw ConnectException("Redis connection failed")
                    }
                    publishedSnippets.add(snippet)
                }
            }

        val jobRepo = InMemoryJobRepository()
        val lintStatusRepo =
            InMemorySnippetLintStatusRepository(
                listOf(
                    SnippetLintStatus(failingSnippet.id, failingSnippet.version, LintStatus.PENDING),
                    SnippetLintStatus(okSnippet.id, okSnippet.version, LintStatus.PENDING),
                ),
            )
        val scheduler = AsyncLintBatchScheduler(partiallyFailingPublisher, jobRepo, lintStatusRepo)

        scheduler.scheduleAll(userId, listOf(failingSnippet, okSnippet), rules)

        assertEquals(
            LintStatus.FAILED,
            lintStatusRepo.findByVersion(failingSnippet.id, failingSnippet.version)?.status,
        )
        assertEquals(
            LintStatus.PENDING,
            lintStatusRepo.findByVersion(okSnippet.id, okSnippet.version)?.status,
        )
        assertEquals(listOf(okSnippet), publishedSnippets)
    }
}
