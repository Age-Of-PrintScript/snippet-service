package age.of.printscript.demo.jobs.infrastructure.controller

import age.of.printscript.demo.jobs.application.GetJobStatus
import age.of.printscript.demo.jobs.application.InMemoryJobRepository
import age.of.printscript.demo.jobs.domain.Job
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.snippet.application.FakePermissionGateway
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.application.testJwt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class JobsControllerTest {
    private val snippet = createDummySnippet()
    private val snippets = InMemorySnippetRepository(listOf(snippet))
    private val jobs = InMemoryJobRepository()
    private val permissions = FakePermissionGateway()
    private val controller = JobsController(GetJobStatus(jobs, snippets, permissions))

    private fun addJob(status: JobStatus): JobId {
        val jobId = JobId("job-1")
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
    fun `returns the status for a job owned by the requester`() {
        val jobId = addJob(JobStatus.PENDING)

        val response = controller.status(jobId.value, testJwt(snippet.ownerId.value))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(JobStatusResponse(jobId.value, "PENDING"), response.body)
    }

    @Test
    fun `returns 403 for a user without access to the job's snippet`() {
        val jobId = addJob(JobStatus.DONE)

        val response = controller.status(jobId.value, testJwt("stranger"))

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun `returns 404 for a missing job`() {
        val response = controller.status("missing", testJwt(snippet.ownerId.value))

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}
