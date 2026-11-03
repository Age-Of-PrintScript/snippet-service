package age.of.printscript.demo.format.application

import age.of.printscript.demo.jobs.application.InMemoryJobRepository
import age.of.printscript.demo.jobs.domain.Job
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CompleteFormatJobTest {
    @Test
    fun `marks job as DONE when job exists`() {
        val jobId = JobId("job-1")
        val job =
            Job(
                id = jobId,
                snippetId = SnippetId("snip-1"),
                snippetVersion = 1,
                userId = UserId("user-1"),
                type = JobType(FORMAT_JOB),
                status = JobStatus.PENDING,
            )
        val jobRepo = InMemoryJobRepository(listOf(job))
        val completeFormatJob = CompleteFormatJob(jobRepo)

        completeFormatJob(jobId)

        val updatedJob = jobRepo.findById(jobId)
        assertNotNull(updatedJob)
        assertEquals(JobStatus.DONE, updatedJob?.status)
    }

    @Test
    fun `does nothing when job is not found`() {
        val jobRepo = InMemoryJobRepository()
        val completeFormatJob = CompleteFormatJob(jobRepo)

        completeFormatJob(JobId("non-existent"))

        assertNull(jobRepo.findById(JobId("non-existent")))
    }
}
