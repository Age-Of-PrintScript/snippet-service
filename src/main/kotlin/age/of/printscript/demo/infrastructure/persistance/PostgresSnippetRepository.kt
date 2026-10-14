package age.of.printscript.demo.infrastructure.persistance

import age.of.printscript.demo.domain.Snippet
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.SnippetRepository
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.infrastructure.persistance.entity.SnippetEntityId
import age.of.printscript.demo.infrastructure.persistance.mapper.toDomain
import age.of.printscript.demo.infrastructure.persistance.mapper.toEntity
import age.of.printscript.demo.infrastructure.persistance.repository.SnippetJpaRepository
import org.springframework.stereotype.Repository
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

    override fun save(snippet: Snippet): Snippet {
        val entity = snippet.toEntity()
        return jpaRepository.save(entity).toDomain()
    }
}
