package age.of.printscript.demo.infrastructure.persistance.mapper

import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.SnippetTest
import age.of.printscript.demo.domain.SnippetTestId
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.infrastructure.persistance.entity.SnippetTestEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class SnippetTestMappersTest {

    @Test
    fun `maps SnippetTestEntity to SnippetTestSummary`() {
        val testId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val now = Instant.now()
        val entity = SnippetTestEntity(
            id = testId,
            snippetId = snippetId,
            ownerId = "auth0|user",
            url = "https://azure.blob/test-1",
            createdAt = now,
            updatedAt = now,
        )

        val summary = entity.toSummary()

        assertEquals(SnippetTestId(testId.toString()), summary.id)
        assertEquals(SnippetId(snippetId.toString()), summary.snippetId)
        assertEquals(UserId("auth0|user"), summary.ownerId)
        assertEquals(now, summary.createdAt)
    }

    @Test
    fun `maps SnippetTestEntity to Domain SnippetTest`() {
        val testId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val entity = SnippetTestEntity(
            id = testId,
            snippetId = snippetId,
            ownerId = "auth0|user",
            url = "https://azure.blob/test-1",
        )
        val inputs = listOf("1", "2")
        val outputs = listOf("3")

        val domain = entity.toDomain(inputs, outputs)

        assertEquals(SnippetTestId(testId.toString()), domain.id)
        assertEquals(SnippetId(snippetId.toString()), domain.snippetId)
        assertEquals(UserId("auth0|user"), domain.ownerId)
        assertEquals(inputs, domain.inputs)
        assertEquals(outputs, domain.outputs)
    }

    @Test
    fun `maps Domain SnippetTest to SnippetTestEntity`() {
        val testId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val test = SnippetTest(
            id = SnippetTestId(testId.toString()),
            snippetId = SnippetId(snippetId.toString()),
            ownerId = UserId("auth0|user"),
            inputs = listOf("10"),
            outputs = listOf("20"),
        )

        val entity = test.toEntity(url = "https://azure.blob/test-1")

        assertEquals(testId, entity.id)
        assertEquals(snippetId, entity.snippetId)
        assertEquals("auth0|user", entity.ownerId)
        assertEquals("https://azure.blob/test-1", entity.url)
    }
}
