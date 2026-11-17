package age.of.printscript.demo.testing.application

import age.of.printscript.demo.jobs.application.InMemoryJobRepository
import age.of.printscript.demo.jobs.domain.Job
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class CompleteTestJobTest {
    private val snippetId = SnippetId("snippet")
    private val testId = SnippetTestId("test")
    private val jobs = InMemoryJobRepository()
    private val tests = InMemorySnippetTestRepository()
    private val complete = CompleteTestJob(jobs, tests)

    @ParameterizedTest
    @EnumSource(value = SnippetTestStatus::class, names = ["PASSED", "FAILED", "ERROR"])
    fun `stores the final test status and completes the job`(status: SnippetTestStatus) {
        val job = addJob("job")
        addPendingTest(job.id)

        complete(job.id, status, "diagnostic")

        assertEquals(status, tests.findById(testId)?.status)
        assertNull(tests.findById(testId)?.activeJobId)
        val expectedJobStatus = if (status == SnippetTestStatus.ERROR) JobStatus.FAILED else JobStatus.DONE
        assertEquals(expectedJobStatus, jobs.findById(job.id)?.status)
        assertEquals("diagnostic", jobs.findById(job.id)?.result)
    }

    @Test
    fun `an old response completes its job without overwriting the current test`() {
        val previous = addJob("previous")
        val current = addJob("current")
        addPendingTest(previous.id)
        tests.markPending(testId, current.id)

        complete(previous.id, SnippetTestStatus.FAILED)

        assertEquals(SnippetTestStatus.PENDING, tests.findById(testId)?.status)
        assertEquals(current.id, tests.findById(testId)?.activeJobId)
        assertEquals(JobStatus.DONE, jobs.findById(previous.id)?.status)
        assertEquals(JobStatus.PENDING, jobs.findById(current.id)?.status)
    }

    @Test
    fun `responses after invalidation cannot restore an obsolete result`() {
        val job = addJob("job")
        addPendingTest(job.id)
        tests.invalidateForSnippet(snippetId)

        complete(job.id, SnippetTestStatus.PASSED)

        assertEquals(SnippetTestStatus.NOT_RUN, tests.findById(testId)?.status)
        assertNull(tests.findById(testId)?.activeJobId)
        assertEquals(JobStatus.DONE, jobs.findById(job.id)?.status)
    }

    @Test
    fun `a repeated response does not replace the first completed result`() {
        val job = addJob("job")
        addPendingTest(job.id)
        complete(job.id, SnippetTestStatus.PASSED)

        complete(job.id, SnippetTestStatus.ERROR, "late error")

        assertEquals(SnippetTestStatus.PASSED, tests.findById(testId)?.status)
        assertEquals(JobStatus.DONE, jobs.findById(job.id)?.status)
        assertNull(jobs.findById(job.id)?.result)
    }

    @Test
    fun `unknown jobs and jobs of another type are ignored`() {
        val job = addJob("format", "FORMAT")
        addPendingTest(job.id)

        complete(JobId("unknown"), SnippetTestStatus.ERROR)
        complete(job.id, SnippetTestStatus.ERROR)

        assertEquals(SnippetTestStatus.PENDING, tests.findById(testId)?.status)
        assertEquals(JobStatus.PENDING, jobs.findById(job.id)?.status)
    }

    private fun addJob(
        id: String,
        type: String = "TEST",
    ): Job = jobs.save(Job(JobId(id), snippetId, 1, UserId("owner"), JobType(type), JobStatus.PENDING))

    private fun addPendingTest(jobId: JobId) {
        tests.save(
            SnippetTest(
                testId,
                snippetId,
                UserId("owner"),
                "Pending test",
                "test-url",
                SnippetTestStatus.PENDING,
                jobId,
            ),
        )
    }
}
