package age.of.printscript.demo.infrastructure.persistance

import age.of.printscript.demo.domain.Job
import age.of.printscript.demo.domain.JobId
import age.of.printscript.demo.domain.JobStatus
import age.of.printscript.demo.domain.JobType
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.infrastructure.persistance.entity.JobEntity
import age.of.printscript.demo.infrastructure.persistance.repository.JobJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional
import java.util.UUID

class PostgresJobRepositoryTest {
    private val jpaRepository: JobJpaRepository = mock(JobJpaRepository::class.java)
    private val repository = PostgresJobRepository(jpaRepository)

    @Test
    fun `findById returns mapped Job when present`() {
        val jobId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val entity =
            JobEntity(
                id = jobId,
                snippetId = snippetId,
                snippetVersion = 1,
                userId = "alice",
                type = "LINT",
                status = "PENDING",
                result = null,
            )
        `when`(jpaRepository.findById(jobId)).thenReturn(Optional.of(entity))

        val result = repository.findById(JobId(jobId.toString()))

        assertEquals(JobId(jobId.toString()), result?.id)
        assertEquals(SnippetId(snippetId.toString()), result?.snippetId)
        assertEquals(JobType("LINT"), result?.type)
        assertEquals(JobStatus.PENDING, result?.status)
    }

    @Test
    fun `findById returns null when entity does not exist`() {
        val jobId = UUID.randomUUID()
        `when`(jpaRepository.findById(jobId)).thenReturn(Optional.empty())

        val result = repository.findById(JobId(jobId.toString()))

        assertNull(result)
    }

    @Test
    fun `save persists and returns mapped Job`() {
        val jobId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val job =
            Job(
                id = JobId(jobId.toString()),
                snippetId = SnippetId(snippetId.toString()),
                snippetVersion = 2,
                userId = UserId("alice"),
                type = JobType("LINT"),
                status = JobStatus.DONE,
                result = null,
            )
        val savedEntity =
            JobEntity(
                id = jobId,
                snippetId = snippetId,
                snippetVersion = 2,
                userId = "alice",
                type = "LINT",
                status = "DONE",
                result = null,
            )
        `when`(jpaRepository.save(any(JobEntity::class.java))).thenReturn(savedEntity)

        val result = repository.save(job)

        assertEquals(job, result)
    }
}
