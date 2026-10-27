package age.of.printscript.demo.infrastructure.persistance

import age.of.printscript.demo.domain.LintStatus
import age.of.printscript.demo.domain.LintWarning
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.SnippetLintStatus
import age.of.printscript.demo.infrastructure.persistance.entity.SnippetLintStatusEntity
import age.of.printscript.demo.infrastructure.persistance.entity.SnippetLintStatusEntityId
import age.of.printscript.demo.infrastructure.persistance.repository.SnippetLintStatusJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional
import java.util.UUID

class PostgresSnippetLintStatusRepositoryTest {
    private val jpaRepository: SnippetLintStatusJpaRepository = mock(SnippetLintStatusJpaRepository::class.java)
    private val repository = PostgresSnippetLintStatusRepository(jpaRepository)

    @Test
    fun `findByVersion returns mapped SnippetLintStatus when present`() {
        val uuid = UUID.randomUUID()
        val entityId = SnippetLintStatusEntityId(snippetId = uuid, version = 1)
        val entity =
            SnippetLintStatusEntity(
                snippetId = uuid,
                version = 1,
                status = "COMPLIANT",
                warnings = "[]",
            )
        `when`(jpaRepository.findById(entityId)).thenReturn(Optional.of(entity))

        val result = repository.findByVersion(SnippetId(uuid.toString()), 1)

        assertEquals(SnippetId(uuid.toString()), result?.snippetId)
        assertEquals(1, result?.version)
        assertEquals(LintStatus.COMPLIANT, result?.status)
    }

    @Test
    fun `findByVersion returns null when entity does not exist`() {
        val uuid = UUID.randomUUID()
        val entityId = SnippetLintStatusEntityId(snippetId = uuid, version = 1)
        `when`(jpaRepository.findById(entityId)).thenReturn(Optional.empty())

        val result = repository.findByVersion(SnippetId(uuid.toString()), 1)

        assertNull(result)
    }

    @Test
    fun `save persists and returns mapped SnippetLintStatus`() {
        val uuid = UUID.randomUUID()
        val status =
            SnippetLintStatus(
                snippetId = SnippetId(uuid.toString()),
                version = 1,
                status = LintStatus.NON_COMPLIANT,
                warnings = listOf(LintWarning("Invalid name", 1, 4)),
            )
        val savedEntity =
            SnippetLintStatusEntity(
                snippetId = uuid,
                version = 1,
                status = "NON_COMPLIANT",
                warnings = """[{"message":"Invalid name","line":1,"column":4}]""",
            )
        `when`(jpaRepository.save(any(SnippetLintStatusEntity::class.java))).thenReturn(savedEntity)

        val result = repository.save(status)

        assertEquals(status, result)
    }
}
