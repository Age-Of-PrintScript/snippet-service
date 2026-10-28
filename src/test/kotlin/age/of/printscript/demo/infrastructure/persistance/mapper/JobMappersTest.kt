package age.of.printscript.demo.infrastructure.persistance.mapper

import age.of.printscript.demo.domain.Job
import age.of.printscript.demo.domain.JobId
import age.of.printscript.demo.domain.JobStatus
import age.of.printscript.demo.domain.JobType
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class JobMappersTest {
    @Test
    fun `maps Domain Job to JobEntity and back to Domain`() {
        val jobId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val domain =
            Job(
                id = JobId(jobId.toString()),
                snippetId = SnippetId(snippetId.toString()),
                snippetVersion = 3,
                userId = UserId("alice"),
                type = JobType("LINT"),
                status = JobStatus.PENDING,
                result = null,
            )

        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(jobId, entity.id)
        assertEquals(snippetId, entity.snippetId)
        assertEquals(3, entity.snippetVersion)
        assertEquals("alice", entity.userId)
        assertEquals("LINT", entity.type)
        assertEquals("PENDING", entity.status)
        assertEquals(domain, mappedBack)
    }
}
