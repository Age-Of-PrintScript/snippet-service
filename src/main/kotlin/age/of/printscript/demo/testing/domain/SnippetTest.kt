package age.of.printscript.demo.testing.domain

import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import java.time.Instant

@JvmInline
value class SnippetTestId(
    val value: String,
)

enum class SnippetTestStatus {
    NOT_RUN,
    PENDING,
    PASSED,
    FAILED,
    ERROR,
}

data class SnippetTest(
    val id: SnippetTestId,
    val snippetId: SnippetId,
    val ownerId: UserId,
    val name: String = "Test",
    val contentUrl: String,
    val status: SnippetTestStatus = SnippetTestStatus.NOT_RUN,
    val activeJobId: JobId? = null,
) {
    constructor(
        id: SnippetTestId,
        snippetId: SnippetId,
        ownerId: UserId,
        contentUrl: String,
        status: SnippetTestStatus = SnippetTestStatus.NOT_RUN,
        activeJobId: JobId? = null,
    ) : this(
        id = id,
        snippetId = snippetId,
        ownerId = ownerId,
        name = "Test",
        contentUrl = contentUrl,
        status = status,
        activeJobId = activeJobId,
    )
}

data class SnippetTestContent(
    val inputs: List<String>,
    val outputs: List<String>,
)

data class SnippetTestSummary(
    val id: SnippetTestId,
    val snippetId: SnippetId,
    val ownerId: UserId,
    val name: String = "Test",
    val createdAt: Instant,
    val contentUrl: String,
    val status: SnippetTestStatus = SnippetTestStatus.NOT_RUN,
) {
    constructor(
        id: SnippetTestId,
        snippetId: SnippetId,
        ownerId: UserId,
        createdAt: Instant,
        contentUrl: String,
        status: SnippetTestStatus = SnippetTestStatus.NOT_RUN,
    ) : this(
        id = id,
        snippetId = snippetId,
        ownerId = ownerId,
        name = "Test",
        createdAt = createdAt,
        contentUrl = contentUrl,
        status = status,
    )
}
