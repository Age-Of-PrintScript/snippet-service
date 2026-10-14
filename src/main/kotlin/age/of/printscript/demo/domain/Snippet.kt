package age.of.printscript.demo.domain

@JvmInline
value class SnippetId(
    val value: String,
)

@JvmInline
value class UserId(
    val value: String,
)

data class Snippet(
    val id: SnippetId,
    val version: Int,
    val title: String,
    val description: String?,
    val ownerId: UserId,
    val language: String,
    val languageVersion: String,
    val contentUrl: String,
    val isLatest: Boolean = true,
)

data class SnippetWithContent(
    val id: SnippetId,
    val version: Int,
    val title: String,
    val description: String?,
    val ownerId: UserId,
    val language: String,
    val languageVersion: String,
    val content: String,
    val isLatest: Boolean = true,
)

fun Snippet.withContent(content: String): SnippetWithContent =
    SnippetWithContent(
        id = id,
        version = version,
        title = title,
        description = description,
        ownerId = ownerId,
        language = language,
        languageVersion = languageVersion,
        content = content,
        isLatest = isLatest,
    )
