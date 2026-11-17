package age.of.printscript.demo.format.infrastructure.listener

import age.of.printscript.demo.format.application.FORMAT_JOB
import age.of.printscript.demo.format.application.port.FormatJobPublisher
import age.of.printscript.demo.jobs.application.FakeJobPublisher
import age.of.printscript.demo.jobs.application.InMemoryJobRepository
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.net.ConnectException

class AsyncFormatBatchSchedulerTest {
    @Test
    fun `scheduleAll saves PENDING job and publishes format job for each snippet`() {
        val userId = UserId("alice")
        val snippet1 = createDummySnippet(id = SnippetId("s-1"), ownerId = userId, version = 1)
        val snippet2 = createDummySnippet(id = SnippetId("s-2"), ownerId = userId, version = 2)
        val rules = mapOf("indent-inside-if" to "4")

        val jobPublisher = FakeJobPublisher()
        val jobRepo = InMemoryJobRepository()
        val scheduler = AsyncFormatBatchScheduler(jobPublisher, jobRepo)

        scheduler.scheduleAll(userId, listOf(snippet1, snippet2), rules)

        assertEquals(2, jobPublisher.publishedFormatJobs.size)

        val firstPublished = jobPublisher.publishedFormatJobs[0]
        assertEquals(snippet1, firstPublished.snippet)
        assertEquals(rules, firstPublished.rules)
        val firstJob = jobRepo.findById(JobId(firstPublished.jobId))
        assertNotNull(firstJob)
        assertEquals(snippet1.id, firstJob?.snippetId)
        assertEquals(snippet1.version, firstJob?.snippetVersion)
        assertEquals(userId, firstJob?.userId)
        assertEquals(JobType(FORMAT_JOB), firstJob?.type)
        assertEquals(JobStatus.PENDING, firstJob?.status)

        val secondPublished = jobPublisher.publishedFormatJobs[1]
        assertEquals(snippet2, secondPublished.snippet)
        assertEquals(rules, secondPublished.rules)
        val secondJob = jobRepo.findById(JobId(secondPublished.jobId))
        assertNotNull(secondJob)
        assertEquals(snippet2.id, secondJob?.snippetId)
    }

    @Test
    fun `scheduleAll marks job status as FAILED and continues when publishing fails for one snippet`() {
        val userId = UserId("alice")
        val failingSnippet = createDummySnippet(id = SnippetId("s-fail"), ownerId = userId, version = 1)
        val okSnippet = createDummySnippet(id = SnippetId("s-ok"), ownerId = userId, version = 1)
        val rules = mapOf("indent-inside-if" to "4")

        val publishedSnippets = mutableListOf<Snippet>()
        val failedJobIds = mutableListOf<String>()
        val partiallyFailingPublisher =
            object : FormatJobPublisher {
                override fun publishFormatJob(
                    jobId: String,
                    snippet: Snippet,
                    rules: Map<String, String?>,
                ) {
                    if (snippet.id == failingSnippet.id) {
                        failedJobIds.add(jobId)
                        throw ConnectException("Redis connection failed")
                    }
                    publishedSnippets.add(snippet)
                }
            }

        val jobRepo = InMemoryJobRepository()
        val scheduler = AsyncFormatBatchScheduler(partiallyFailingPublisher, jobRepo)

        scheduler.scheduleAll(userId, listOf(failingSnippet, okSnippet), rules)

        assertEquals(1, failedJobIds.size)
        val failedJob = jobRepo.findById(JobId(failedJobIds.first()))
        assertEquals(JobStatus.FAILED, failedJob?.status)
        assertEquals(listOf(okSnippet), publishedSnippets)
    }
}
