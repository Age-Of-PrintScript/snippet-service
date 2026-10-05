package age.of.printscript.demo.infrastructure.persistance.mapper

import age.of.printscript.demo.domain.Snippet
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.infrastructure.persistance.entity.SnippetEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class SnippetMappersTest {

    @Test
    fun `maps SnippetEntity to Domain Snippet`() {
        val id = UUID.randomUUID()
        val entity = SnippetEntity(
            id = id,
            version = 2,
            ownerId = "auth0|123",
            title = "My Snippet",
            description = "Some description",
            language = "PrintScript",
            languageVersion = "1.1",
            url = "https://azure.blob/snippet-1",
            isLatest = true,
        )

        val domain = entity.toDomain(content = "let a: number = 5;")

        assertEquals(SnippetId(id.toString()), domain.id)
        assertEquals(2, domain.version)
        assertEquals("auth0|123", domain.ownerId.value)
        assertEquals("My Snippet", domain.title)
        assertEquals("Some description", domain.description)
        assertEquals("PrintScript", domain.language)
        assertEquals("1.1", domain.languageVersion)
        assertEquals("let a: number = 5;", domain.content)
        assertTrue(domain.isLatest)
    }

    @Test
    fun `maps Domain Snippet to SnippetEntity`() {
        val id = UUID.randomUUID()
        val snippet = Snippet(
            id = SnippetId(id.toString()),
            version = 1,
            title = "My Snippet",
            description = null,
            ownerId = UserId("auth0|123"),
            language = "PrintScript",
            languageVersion = "1.1",
            content = "let a: number = 5;",
            isLatest = true,
        )

        val entity = snippet.toEntity(url = "https://azure.blob/snippet-1")

        assertEquals(id, entity.id)
        assertEquals(1, entity.version)
        assertEquals("auth0|123", entity.ownerId)
        assertEquals("My Snippet", entity.title)
        assertEquals(null, entity.description)
        assertEquals("PrintScript", entity.language)
        assertEquals("1.1", entity.languageVersion)
        assertEquals("https://azure.blob/snippet-1", entity.url)
        assertTrue(entity.isLatest)
    }
}
