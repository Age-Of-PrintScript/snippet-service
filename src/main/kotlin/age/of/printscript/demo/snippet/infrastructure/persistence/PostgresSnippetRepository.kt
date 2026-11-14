package age.of.printscript.demo.snippet.infrastructure.persistence

import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.snippet.infrastructure.persistence.entity.SnippetEntityId
import age.of.printscript.demo.snippet.infrastructure.persistence.mapper.toDomain
import age.of.printscript.demo.snippet.infrastructure.persistence.mapper.toEntity
import age.of.printscript.demo.snippet.infrastructure.persistence.repository.SnippetJpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
class PostgresSnippetRepository(
    private val jpaRepository: SnippetJpaRepository,
) : SnippetRepository {
    override fun findLatest(id: SnippetId): Snippet? {
        val uuid = UUID.fromString(id.value)
        return jpaRepository
            .findByIdAndIsLatestTrue(uuid)
            ?.toDomain()
    }

    override fun findByVersion(
        id: SnippetId,
        version: Int,
    ): Snippet? {
        val uuid = UUID.fromString(id.value)
        val entityId = SnippetEntityId(id = uuid, version = version)
        return jpaRepository
            .findById(entityId)
            .orElse(null)
            ?.toDomain()
    }

    override fun findAllByOwner(ownerId: UserId): List<Snippet> =
        jpaRepository
            .findAllByOwnerIdAndIsLatestTrue(ownerId.value)
            .map { it.toDomain() }

    @Transactional
    override fun save(snippet: Snippet): Snippet {
        val uuid = UUID.fromString(snippet.id.value)
        if (snippet.isLatest) {
            jpaRepository.findByIdAndIsLatestTrue(uuid)?.let { currentLatest ->
                if (currentLatest.version != snippet.version) {
                    jpaRepository.save(currentLatest.toDomain().copy(isLatest = false).toEntity())
                }
            }
        }
        val entity = snippet.toEntity()
        return jpaRepository.save(entity).toDomain()
    }
}
