package age.of.printscript.demo.jobs.application

import age.of.printscript.demo.jobs.domain.Job
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.snippet.application.FakePermissionGateway
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetJobStatusTest {
    private val snippet = createDummySnippet()
    private val snippets = InMemorySnippetRepository(listOf(snippet))
    private val reader = UserId("reader")
    private val permissions = FakePermissionGateway(setOf(snippet.id to reader))
    private val jobs = InMemoryJobRepository()
    private val getJobStatus = GetJobStatus(jobs, snippets, permissions)

    private fun addJob(status: JobStatus): JobId {
        val jobId = JobId("job-${status.name}")
        jobs.save(
            Job(
                id = jobId,
                snippetId = snippet.id,
                snippetVersion = snippet.version,
                userId = snippet.ownerId,
                type = JobType("FORMAT"),
                status = status,
            ),
        )
        return jobId
    }

    @Test
    fun `returns the job status for the owner`() {
        val jobId = addJob(JobStatus.PENDING)

        val result = getJobStatus(jobId, snippet.ownerId)

        assertEquals(GetJobStatusResult.Success(JobStatus.PENDING), result)
    }

    @Test
    fun `users with read access can poll the job status`() {
        val jobId = addJob(JobStatus.DONE)

        val result = getJobStatus(jobId, reader)

        assertEquals(GetJobStatusResult.Success(JobStatus.DONE), result)
    }

    @Test
    fun `returns forbidden when the user has no access to the snippet`() {
        val jobId = addJob(JobStatus.PENDING)

        val result = getJobStatus(jobId, UserId("stranger"))

        assertEquals(GetJobStatusResult.Forbidden, result)
    }

    @Test
    fun `returns not found when the job does not exist`() {
        val result = getJobStatus(JobId("non-existent"), snippet.ownerId)

        assertEquals(GetJobStatusResult.NotFound, result)
    }
}
