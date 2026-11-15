package age.of.printscript.demo.testing.infrastructure.persistence.mapper

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.domain.TestRun
import age.of.printscript.demo.testing.domain.TestRunId
import age.of.printscript.demo.testing.domain.TestRunStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class TestRunMappersTest {
    @Test
    fun `maps test run between domain and entity`() {
        val now = Instant.parse("2026-11-15T12:00:00Z")
        val testRun =
            TestRun(
                id = TestRunId(UUID.randomUUID().toString()),
                snippetId = SnippetId(UUID.randomUUID().toString()),
                snippetVersion = 3,
                requestedBy = UserId("alice"),
                status = TestRunStatus.COMPLETED_WITH_ERRORS,
                createdAt = now,
                startedAt = now,
                finishedAt = now,
            )

        val entity = testRun.toEntity()

        assertEquals("COMPLETED_WITH_ERRORS", entity.status)
        assertEquals(testRun, entity.toDomain())
    }
}
