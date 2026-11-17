package age.of.printscript.demo.testing.infrastructure.persistence

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.application.SnippetTestPersistenceException
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.infrastructure.persistence.entity.SnippetTestEntity
import age.of.printscript.demo.testing.infrastructure.persistence.repository.SnippetTestJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.dao.DataIntegrityViolationException
import java.time.Instant
import java.util.UUID

class PostgresSnippetTestRepositoryTest {
    private val jpaRepository: SnippetTestJpaRepository = mock(SnippetTestJpaRepository::class.java)
    private val repository = PostgresSnippetTestRepository(jpaRepository)

    @Test
    fun `save persists test metadata and returns its summary`() {
        val testId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val createdAt = Instant.parse("2026-11-14T12:00:00Z")
        val contentUrl = "https://asset/v1/asset/snippet-tests/$testId"
        val test =
            SnippetTest(
                id = SnippetTestId(testId.toString()),
                snippetId = SnippetId(snippetId.toString()),
                ownerId = UserId("alice"),
                contentUrl = contentUrl,
            )
        val savedEntity =
            SnippetTestEntity(
                id = testId,
                snippetId = snippetId,
                ownerId = "alice",
                url = contentUrl,
                createdAt = createdAt,
            )
        doReturn(savedEntity).`when`(jpaRepository).save(any())

        val result = repository.save(test)

        assertEquals(SnippetTestId(testId.toString()), result.id)
        assertEquals(SnippetId(snippetId.toString()), result.snippetId)
        assertEquals(UserId("alice"), result.ownerId)
        assertEquals(createdAt, result.createdAt)
    }

    @Test
    fun `save translates database errors to application exception`() {
        val test =
            SnippetTest(
                id = SnippetTestId(UUID.randomUUID().toString()),
                snippetId = SnippetId(UUID.randomUUID().toString()),
                ownerId = UserId("alice"),
                contentUrl = "https://asset/v1/asset/snippet-tests/test-1",
            )
        doThrow(DataIntegrityViolationException("Database unavailable")).`when`(jpaRepository).save(any())

        org.junit.jupiter.api.Assertions.assertThrows(SnippetTestPersistenceException::class.java) {
            repository.save(test)
        }
    }

    @Test
    fun `deleteById delegates to jpaRepository`() {
        val testId = UUID.randomUUID()

        repository.deleteById(SnippetTestId(testId.toString()))

        verify(jpaRepository).deleteById(testId)
    }

    @Test
    fun `deleteById translates database errors to application exception`() {
        val testId = UUID.randomUUID()
        doThrow(DataIntegrityViolationException("Database unavailable")).`when`(jpaRepository).deleteById(testId)

        org.junit.jupiter.api.Assertions.assertThrows(SnippetTestPersistenceException::class.java) {
            repository.deleteById(SnippetTestId(testId.toString()))
        }
    }
}
