package age.of.printscript.demo.lint.infrastructure.persistence

import age.of.printscript.demo.lint.domain.SnippetLintStatus
import age.of.printscript.demo.lint.domain.SnippetLintStatusRepository
import age.of.printscript.demo.lint.infrastructure.persistence.entity.SnippetLintStatusEntityId
import age.of.printscript.demo.lint.infrastructure.persistence.mapper.toDomain
import age.of.printscript.demo.lint.infrastructure.persistence.mapper.toEntity
import age.of.printscript.demo.lint.infrastructure.persistence.repository.SnippetLintStatusJpaRepository
import age.of.printscript.demo.snippet.domain.SnippetId
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
