package age.of.printscript.demo.application

import age.of.printscript.demo.domain.Snippet
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId

fun createDummySnippet(
    ownerId: UserId = UserId("alice"),
    id: SnippetId = SnippetId("snippet-1"),
    version: Int = 1,
    contentUrl: String = "https://azure.blob/snippets/snippet-1-1.ps",
) = Snippet(
    id = id,
    version = version,
    title = "Test Snippet",
    description = null,
    ownerId = ownerId,
    language = "PrintScript",
    languageVersion = "1.1",
    contentUrl = contentUrl,
    isLatest = true,
)
