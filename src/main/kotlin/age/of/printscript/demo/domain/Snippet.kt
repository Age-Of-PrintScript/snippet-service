package age.of.printscript.demo.domain


@JvmInline
value class SnippetId(val value: String)

@JvmInline
value class UserId(val value: String)

data class Snippet(
    val id: SnippetId,
    val title: String,
    val description: String?,
    val ownerId: UserId,
    val language: String,
    val version: String,
    val content: String,
)
