package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import org.springframework.security.oauth2.jwt.Jwt

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

fun testJwt(subject: String) =
    Jwt
        .withTokenValue("test-token")
        .header("alg", "none") // header del jwt
        .subject(subject) // usuario dueño del ticket
        .build() // firma
