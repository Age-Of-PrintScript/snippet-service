package age.of.printscript.demo.testing.infrastructure.persistence

import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.testing.application.SnippetTestPersistenceException
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestRepository
import age.of.printscript.demo.testing.domain.SnippetTestStatus
import age.of.printscript.demo.testing.domain.SnippetTestSummary
import age.of.printscript.demo.testing.infrastructure.persistence.mapper.toDomain
import age.of.printscript.demo.testing.infrastructure.persistence.mapper.toEntity
import age.of.printscript.demo.testing.infrastructure.persistence.mapper.toSummary
import age.of.printscript.demo.testing.infrastructure.persistence.repository.SnippetTestJpaRepository
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class PostgresSnippetTestRepository(
    private val jpaRepository: SnippetTestJpaRepository,
) : SnippetTestRepository {
    override fun findAllBySnippetId(snippetId: SnippetId): List<SnippetTestSummary> {
        val uuid = UUID.fromString(snippetId.value)
        return jpaRepository.findAllBySnippetId(uuid).map { it.toSummary() }
    }

    override fun findById(id: SnippetTestId): SnippetTest? {
        val uuid = UUID.fromString(id.value)
        return jpaRepository.findById(uuid).orElse(null)?.toDomain()
    }

    override fun markPending(
        id: SnippetTestId,
        jobId: JobId,
    ) {
        val updated = jpaRepository.markPending(UUID.fromString(id.value), UUID.fromString(jobId.value), Instant.now())
        check(updated == 1) { "Test ${id.value} no longer exists" }
    }

    override fun save(test: SnippetTest): SnippetTestSummary =
        try {
            jpaRepository
                .save(test.toEntity())
                .toSummary()
        } catch (failure: DataAccessException) {
            throw SnippetTestPersistenceException(failure)
        }

    override fun completeIfActive(
        jobId: JobId,
        status: SnippetTestStatus,
    ) {
        jpaRepository.completeIfActive(UUID.fromString(jobId.value), status.name, Instant.now())
    }

    override fun invalidateForSnippet(snippetId: SnippetId) {
        jpaRepository.invalidateForSnippet(UUID.fromString(snippetId.value), Instant.now())
    }

    override fun deleteById(id: SnippetTestId) {
        try {
            jpaRepository.deleteById(UUID.fromString(id.value))
        } catch (failure: DataAccessException) {
            throw SnippetTestPersistenceException(failure)
        }
    }
}
