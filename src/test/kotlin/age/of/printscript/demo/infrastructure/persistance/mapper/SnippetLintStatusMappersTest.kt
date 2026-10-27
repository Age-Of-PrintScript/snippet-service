package age.of.printscript.demo.infrastructure.persistance.mapper

import age.of.printscript.demo.domain.LintStatus
import age.of.printscript.demo.domain.LintWarning
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.SnippetLintStatus
import age.of.printscript.demo.infrastructure.persistance.entity.SnippetLintStatusEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class SnippetLintStatusMappersTest {
    @Test
    fun `maps Domain SnippetLintStatus to Entity and back to Domain`() {
        val snippetId = UUID.randomUUID()
        val domain =
            SnippetLintStatus(
                snippetId = SnippetId(snippetId.toString()),
                version = 2,
                status = LintStatus.NON_COMPLIANT,
                warnings = listOf(LintWarning(message = "Bad identifier", line = 3, column = 5)),
            )

        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(snippetId, entity.snippetId)
        assertEquals(2, entity.version)
        assertEquals("NON_COMPLIANT", entity.status)
        assertEquals(domain, mappedBack)
    }

    @Test
    fun `maps Entity with null or blank warnings to empty list`() {
        val snippetId = UUID.randomUUID()
        val nullWarningsEntity =
            SnippetLintStatusEntity(
                snippetId = snippetId,
                version = 1,
                status = "PENDING",
                warnings = null,
            )
        val blankWarningsEntity =
            SnippetLintStatusEntity(
                snippetId = snippetId,
                version = 1,
                status = "COMPLIANT",
                warnings = "   ",
            )

        assertTrue(nullWarningsEntity.toDomain().warnings.isEmpty())
        assertTrue(blankWarningsEntity.toDomain().warnings.isEmpty())
    }
}
