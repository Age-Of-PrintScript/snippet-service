package age.of.printscript.demo.testing.domain

import age.of.printscript.demo.jobs.domain.JobId
import java.time.Instant

@JvmInline
value class TestExecutionId(
    val value: String,
)

enum class TestExecutionStatus {
    PENDING,
    RUNNING,
    PASSED,
    FAILED,
    ERROR,
    TIMED_OUT,
}

data class TestExecution(
    val id: TestExecutionId,
    val runId: TestRunId,
    val testId: SnippetTestId,
    val jobId: JobId,
    val testUrl: String,
    val status: TestExecutionStatus,
    val createdAt: Instant = Instant.now(),
    val startedAt: Instant? = null,
    val finishedAt: Instant? = null,
)
