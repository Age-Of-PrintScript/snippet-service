package age.of.printscript.demo.domain

import java.time.Instant

@JvmInline
value class SnippetTestId(
    val value: String,
)

data class SnippetTest(
    val id: SnippetTestId,
    val snippetId: SnippetId,
    val ownerId: UserId,
    val inputs: List<String>,
    val outputs: List<String>,
)

data class SnippetTestSummary(
    val id: SnippetTestId,
    val snippetId: SnippetId,
    val ownerId: UserId,
    val createdAt: Instant,
)
