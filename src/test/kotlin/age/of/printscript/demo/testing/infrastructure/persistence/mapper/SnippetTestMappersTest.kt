package age.of.printscript.demo.testing.infrastructure.persistence.mapper

import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestStatus
import age.of.printscript.demo.testing.infrastructure.persistence.entity.SnippetTestEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class SnippetTestMappersTest {
    @Test
    fun `maps SnippetTestEntity to SnippetTestSummary`() {
        val testId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val now = Instant.now()
        val entity =
            SnippetTestEntity(
                id = testId,
                snippetId = snippetId,
                ownerId = "auth0|user",
                url = "https://azure.blob/test-1",
                createdAt = now,
                updatedAt = now,
                status = "PASSED",
            )

        val summary = entity.toSummary()

        assertEquals(SnippetTestId(testId.toString()), summary.id)
        assertEquals(SnippetId(snippetId.toString()), summary.snippetId)
        assertEquals(UserId("auth0|user"), summary.ownerId)
        assertEquals(now, summary.createdAt)
        assertEquals(SnippetTestStatus.PASSED, summary.status)
    }

    @Test
    fun `maps SnippetTestEntity to Domain SnippetTest`() {
        val testId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val jobId = UUID.randomUUID()
        val entity =
            SnippetTestEntity(
                id = testId,
                snippetId = snippetId,
                ownerId = "auth0|user",
                url = "https://azure.blob/test-1",
                status = "PENDING",
                activeJobId = jobId,
            )
        val domain = entity.toDomain()

        assertEquals(SnippetTestId(testId.toString()), domain.id)
        assertEquals(SnippetId(snippetId.toString()), domain.snippetId)
        assertEquals(UserId("auth0|user"), domain.ownerId)
        assertEquals("https://azure.blob/test-1", domain.contentUrl)
        assertEquals(SnippetTestStatus.PENDING, domain.status)
        assertEquals(JobId(jobId.toString()), domain.activeJobId)
    }

    @Test
    fun `maps Domain SnippetTest to SnippetTestEntity`() {
        val testId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val test =
            SnippetTest(
                id = SnippetTestId(testId.toString()),
                snippetId = SnippetId(snippetId.toString()),
                ownerId = UserId("auth0|user"),
                contentUrl = "https://azure.blob/test-1",
            )

        val entity = test.toEntity()

        assertEquals(testId, entity.id)
        assertEquals(snippetId, entity.snippetId)
        assertEquals("auth0|user", entity.ownerId)
        assertEquals("https://azure.blob/test-1", entity.url)
        assertEquals("NOT_RUN", entity.status)
        assertNull(entity.activeJobId)
    }

    @Test
    fun `preserves the latest status and active job when mapping to persistence and back`() {
        val test =
            SnippetTest(
                id = SnippetTestId(UUID.randomUUID().toString()),
                snippetId = SnippetId(UUID.randomUUID().toString()),
                ownerId = UserId("auth0|user"),
                contentUrl = "https://azure.blob/test-1",
                status = SnippetTestStatus.FAILED,
                activeJobId = JobId(UUID.randomUUID().toString()),
            )

        val entity = test.toEntity()

        assertEquals("FAILED", entity.status)
        assertEquals(test.activeJobId?.value, entity.activeJobId?.toString())
        assertEquals(test, entity.toDomain())
    }
}
