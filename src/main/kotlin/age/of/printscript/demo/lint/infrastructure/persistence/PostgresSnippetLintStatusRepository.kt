package age.of.printscript.demo.lint.infrastructure.persistence

import age.of.printscript.demo.lint.domain.LintStatus
import age.of.printscript.demo.lint.domain.SnippetLintStatus
import age.of.printscript.demo.lint.domain.SnippetLintStatusRepository
import age.of.printscript.demo.lint.infrastructure.persistence.entity.SnippetLintStatusEntityId
import age.of.printscript.demo.lint.infrastructure.persistence.mapper.toDomain
import age.of.printscript.demo.lint.infrastructure.persistence.mapper.toEntity
import age.of.printscript.demo.lint.infrastructure.persistence.repository.SnippetLintStatusJpaRepository
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
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

    override fun findStatuses(snippets: List<Snippet>): Map<SnippetId, LintStatus> {
        if (snippets.isEmpty()) return emptyMap()
        val ids = snippets.map { SnippetLintStatusEntityId(UUID.fromString(it.id.value), it.version) }
        return jpaRepository
            .findAllById(ids)
            .map { it.toDomain() }
            .associate { it.snippetId to it.status }
    }

    override fun save(lintStatus: SnippetLintStatus): SnippetLintStatus {
        val entity = lintStatus.toEntity()
        return jpaRepository.save(entity).toDomain()
    }

    @Transactional
    override fun markAllPending(snippets: List<Snippet>) {
        val entities =
            snippets.map { snippet ->
                SnippetLintStatus(
                    snippetId = snippet.id,
                    version = snippet.version,
                    status = LintStatus.PENDING,
                ).toEntity()
            }
        jpaRepository.saveAll(entities)
    }
}
