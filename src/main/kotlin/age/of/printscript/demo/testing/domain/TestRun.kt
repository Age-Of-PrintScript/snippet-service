package age.of.printscript.demo.testing.domain

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import java.time.Instant

@JvmInline
value class TestRunId(
    val value: String,
)

enum class TestRunStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    COMPLETED_WITH_ERRORS,
}

data class TestRun(
    val id: TestRunId,
    val snippetId: SnippetId,
    val snippetVersion: Int,
    val requestedBy: UserId,
    val status: TestRunStatus,
    val createdAt: Instant = Instant.now(),
    val startedAt: Instant? = null,
    val finishedAt: Instant? = null,
)
