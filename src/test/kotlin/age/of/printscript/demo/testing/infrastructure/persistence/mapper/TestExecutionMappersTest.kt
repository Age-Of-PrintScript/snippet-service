package age.of.printscript.demo.testing.infrastructure.persistence.mapper

import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.TestExecution
import age.of.printscript.demo.testing.domain.TestExecutionId
import age.of.printscript.demo.testing.domain.TestExecutionStatus
import age.of.printscript.demo.testing.domain.TestRunId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class TestExecutionMappersTest {
    @Test
    fun `maps test execution snapshots between domain and entity`() {
        val now = Instant.parse("2026-11-15T12:00:00Z")
        val execution =
            TestExecution(
                id = TestExecutionId(UUID.randomUUID().toString()),
                runId = TestRunId(UUID.randomUUID().toString()),
                testId = SnippetTestId(UUID.randomUUID().toString()),
                jobId = JobId(UUID.randomUUID().toString()),
                testUrl = "https://asset/v1/asset/snippet-tests/test-1",
                status = TestExecutionStatus.FAILED,
                isStale = true,
                createdAt = now,
                startedAt = now,
                finishedAt = now,
            )

        val entity = execution.toEntity()

        assertEquals(execution.testUrl, entity.testUrl)
        assertEquals(true, entity.isStale)
        assertEquals(execution, entity.toDomain())
    }
}
