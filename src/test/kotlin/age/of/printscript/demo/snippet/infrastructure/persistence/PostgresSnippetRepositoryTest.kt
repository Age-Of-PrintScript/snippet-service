package age.of.printscript.demo.snippet.infrastructure.persistence

import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.snippet.infrastructure.persistence.entity.SnippetEntity
import age.of.printscript.demo.snippet.infrastructure.persistence.entity.SnippetEntityId
import age.of.printscript.demo.snippet.infrastructure.persistence.repository.SnippetJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional
import java.util.UUID

class PostgresSnippetRepositoryTest {
    private val jpaRepository: SnippetJpaRepository = mock(SnippetJpaRepository::class.java)
    private val repository = PostgresSnippetRepository(jpaRepository)

    @Test
    fun `findLatest returns mapped domain snippet when found`() {
        val uuid = UUID.randomUUID()
        val entity =
            SnippetEntity(
                id = uuid,
                version = 1,
                ownerId = "alice",
                title = "My Snippet",
                description = null,
                language = "PrintScript",
                languageVersion = "1.1",
                url = "https://azure.blob/1",
                isLatest = true,
            )
        `when`(jpaRepository.findByIdAndIsLatestTrue(uuid)).thenReturn(entity)

        val result = repository.findLatest(SnippetId(uuid.toString()))

        assertEquals(SnippetId(uuid.toString()), result?.id)
        assertEquals("My Snippet", result?.title)
    }

    @Test
    fun `findLatest returns null when not found`() {
        val uuid = UUID.randomUUID()
        `when`(jpaRepository.findByIdAndIsLatestTrue(uuid)).thenReturn(null)

        val result = repository.findLatest(SnippetId(uuid.toString()))

        assertNull(result)
    }

    @Test
    fun `findByVersion returns mapped domain snippet`() {
        val uuid = UUID.randomUUID()
        val entityId = SnippetEntityId(id = uuid, version = 2)
        val entity =
            SnippetEntity(
                id = uuid,
                version = 2,
                ownerId = "alice",
                title = "Snippet V2",
                description = null,
                language = "PrintScript",
                languageVersion = "1.1",
                url = "https://azure.blob/2",
                isLatest = true,
            )
        `when`(jpaRepository.findById(entityId)).thenReturn(Optional.of(entity))

        val result = repository.findByVersion(SnippetId(uuid.toString()), version = 2)

        assertEquals(2, result?.version)
        assertEquals("Snippet V2", result?.title)
    }

    @Test
    fun `findAllByOwner returns list of mapped domain snippets`() {
        val uuid = UUID.randomUUID()
        val entity =
            SnippetEntity(
                id = uuid,
                version = 1,
                ownerId = "alice",
                title = "Snippet",
                description = null,
                language = "PrintScript",
                languageVersion = "1.1",
                url = "https://azure.blob/1",
                isLatest = true,
            )
        `when`(jpaRepository.findAllByOwnerIdAndIsLatestTrue("alice")).thenReturn(listOf(entity))

        val result = repository.findAllByOwner(UserId("alice"))

        assertEquals(1, result.size)
        assertEquals("Snippet", result.first().title)
    }

    @Test
    fun `save persists entity and returns mapped domain snippet`() {
        val uuid = UUID.randomUUID()
        val snippet =
            Snippet(
                id = SnippetId(uuid.toString()),
                version = 1,
                title = "Saved Snippet",
                description = null,
                ownerId = UserId("alice"),
                language = "PrintScript",
                languageVersion = "1.1",
                contentUrl = "https://azure.blob/placeholder",
                isLatest = true,
            )
        val savedEntity =
            SnippetEntity(
                id = uuid,
                version = 1,
                ownerId = "alice",
                title = "Saved Snippet",
                description = null,
                language = "PrintScript",
                languageVersion = "1.1",
                url = "https://azure.blob/placeholder",
                isLatest = true,
            )
        org.mockito.Mockito
            .doReturn(savedEntity)
            .`when`(jpaRepository)
            .save(org.mockito.ArgumentMatchers.any())

        val result = repository.save(snippet)

        assertEquals("Saved Snippet", result.title)
    }

    @Test
    fun `save marks previous latest version as not latest before saving new version`() {
        val uuid = UUID.randomUUID()
        val previousLatest =
            SnippetEntity(
                id = uuid,
                version = 1,
                ownerId = "alice",
                title = "Saved Snippet",
                description = null,
                language = "PrintScript",
                languageVersion = "1.1",
                url = "https://azure.blob/1",
                isLatest = true,
            )
        val newSnippet =
            Snippet(
                id = SnippetId(uuid.toString()),
                version = 2,
                title = "Saved Snippet",
                description = null,
                ownerId = UserId("alice"),
                language = "PrintScript",
                languageVersion = "1.1",
                contentUrl = "https://azure.blob/2",
                isLatest = true,
            )
        val savedV2Entity =
            SnippetEntity(
                id = uuid,
                version = 2,
                ownerId = "alice",
                title = "Saved Snippet",
                description = null,
                language = "PrintScript",
                languageVersion = "1.1",
                url = "https://azure.blob/2",
                isLatest = true,
            )
        `when`(jpaRepository.findByIdAndIsLatestTrue(uuid)).thenReturn(previousLatest)
        org.mockito.Mockito
            .doReturn(savedV2Entity)
            .`when`(jpaRepository)
            .save(org.mockito.ArgumentMatchers.any())

        val result = repository.save(newSnippet)

        assertEquals(2, result.version)
        assertTrue(result.isLatest)
        val captor = ArgumentCaptor.forClass(SnippetEntity::class.java)
        verify(jpaRepository, times(2)).save(captor.capture())
        val savedEntities = captor.allValues
        assertEquals(1, savedEntities[0].version)
        assertFalse(savedEntities[0].isLatest)
        assertEquals(2, savedEntities[1].version)
        assertTrue(savedEntities[1].isLatest)
    }
}
