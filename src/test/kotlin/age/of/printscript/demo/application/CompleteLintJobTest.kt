package age.of.printscript.demo.application

import age.of.printscript.demo.domain.Job
import age.of.printscript.demo.domain.JobId
import age.of.printscript.demo.domain.JobStatus
import age.of.printscript.demo.domain.JobType
import age.of.printscript.demo.domain.LintStatus
import age.of.printscript.demo.domain.LintWarning
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CompleteLintJobTest {
    @Test
    fun `marks job as DONE and saves COMPLIANT status when warnings list is empty`() {
        val jobId = JobId("job-1")
        val snippetId = SnippetId("snip-1")
        val job =
            Job(
                id = jobId,
                snippetId = snippetId,
                snippetVersion = 1,
                userId = UserId("user-1"),
                type = JobType(LINT_JOB),
                status = JobStatus.PENDING,
            )
        val jobRepo = InMemoryJobRepository(listOf(job))
        val lintStatusRepo = InMemorySnippetLintStatusRepository()
        val completeLintJob = CompleteLintJob(lintStatusRepo, jobRepo)

        completeLintJob(jobId, emptyList())

        val updatedJob = jobRepo.findById(jobId)
        assertNotNull(updatedJob)
        assertEquals(JobStatus.DONE, updatedJob?.status)

        val savedStatus = lintStatusRepo.findByVersion(snippetId, 1)
        assertNotNull(savedStatus)
        assertEquals(LintStatus.COMPLIANT, savedStatus?.status)
        assertEquals(emptyList<LintWarning>(), savedStatus?.warnings)
    }

    @Test
    fun `marks job as DONE and saves NON_COMPLIANT status with warnings when warnings list is not empty`() {
        val jobId = JobId("job-2")
        val snippetId = SnippetId("snip-2")
        val job =
            Job(
                id = jobId,
                snippetId = snippetId,
                snippetVersion = 2,
                userId = UserId("user-1"),
                type = JobType(LINT_JOB),
                status = JobStatus.PENDING,
            )
        val jobRepo = InMemoryJobRepository(listOf(job))
        val lintStatusRepo = InMemorySnippetLintStatusRepository()
        val completeLintJob = CompleteLintJob(lintStatusRepo, jobRepo)

        val warnings = listOf(LintWarning("Invalid identifier", 1, 5))
        completeLintJob(jobId, warnings)

        val updatedJob = jobRepo.findById(jobId)
        assertNotNull(updatedJob)
        assertEquals(JobStatus.DONE, updatedJob?.status)

        val savedStatus = lintStatusRepo.findByVersion(snippetId, 2)
        assertNotNull(savedStatus)
        assertEquals(LintStatus.NON_COMPLIANT, savedStatus?.status)
        assertEquals(warnings, savedStatus?.warnings)
    }

    @Test
    fun `does nothing when job is not found`() {
        val jobRepo = InMemoryJobRepository()
        val lintStatusRepo = InMemorySnippetLintStatusRepository()
        val completeLintJob = CompleteLintJob(lintStatusRepo, jobRepo)

        completeLintJob(JobId("non-existent"), emptyList())

        assertNull(jobRepo.findById(JobId("non-existent")))
        assertNull(lintStatusRepo.findByVersion(SnippetId("any"), 1))
    }
}
