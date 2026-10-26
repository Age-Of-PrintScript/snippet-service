package age.of.printscript.demo.infrastructure.persistance

import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.SnippetLintStatus
import age.of.printscript.demo.domain.SnippetLintStatusRepository
import age.of.printscript.demo.infrastructure.persistance.entity.SnippetLintStatusEntityId
import age.of.printscript.demo.infrastructure.persistance.mapper.toDomain
import age.of.printscript.demo.infrastructure.persistance.mapper.toEntity
import age.of.printscript.demo.infrastructure.persistance.repository.SnippetLintStatusJpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class PostgresSnippetLintStatusRepository(
    private val jpaRepository: SnippetLintStatusJpaRepository,
) : SnippetLintStatusRepository {
    override fun findByVersion(
        snippetId: SnippetId,
        version: Int,
    ): SnippetLintStatus? {
        val uuid = UUID.fromString(snippetId.value)
        val entityId = SnippetLintStatusEntityId(snippetId = uuid, version = version)
        return jpaRepository
            .findById(entityId)
            .orElse(null)
            ?.toDomain()
    }

    override fun save(lintStatus: SnippetLintStatus): SnippetLintStatus {
        val entity = lintStatus.toEntity()
        return jpaRepository.save(entity).toDomain()
    }
}
