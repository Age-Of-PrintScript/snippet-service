package age.of.printscript.demo.testing.infrastructure.listener

import age.of.printscript.demo.jobs.application.InMemoryJobRepository
import age.of.printscript.demo.jobs.domain.Job
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.jobs.infrastructure.listener.JobResponseHandler
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.application.CompleteTestJob
import age.of.printscript.demo.testing.application.InMemorySnippetTestRepository
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestStatus
import age.of.printscript.jobs.JobSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestJobResponseStrategyTest {
    @Test
    fun `serialized worker responses update tests through the response handler`() {
        val scenarios =
            listOf(
                """{"type":"TEST","jobId":"job","success":true}""" to SnippetTestStatus.PASSED,
                """{"type":"TEST","jobId":"job","success":false,"message":"mismatch"}""" to SnippetTestStatus.FAILED,
                """{"type":"SYSTEM_ERROR","jobId":"job","reason":"unavailable asset"}""" to SnippetTestStatus.ERROR,
            )
        scenarios.forEach { (payload, status) ->
            val job = Job(JobId("job"), SnippetId("snippet"), 1, UserId("owner"), JobType("TEST"), JobStatus.PENDING)
            val jobs = InMemoryJobRepository(listOf(job))
            val tests = InMemorySnippetTestRepository()
            val test = SnippetTest(SnippetTestId("test"), job.snippetId, job.userId, "test-url")
            tests.save(test)
            tests.markPending(test.id, job.id)
            val handler = JobResponseHandler(listOf(TestJobResponseStrategy(CompleteTestJob(jobs, tests))))

            handler.handle(JobSerializer.deserializeResponse(payload))

            assertEquals(status, tests.findById(test.id)?.status)
            val jobStatus = if (status == SnippetTestStatus.ERROR) JobStatus.FAILED else JobStatus.DONE
            assertEquals(jobStatus, jobs.findById(job.id)?.status)
        }
    }
}
