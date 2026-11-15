package age.of.printscript.demo.testing.infrastructure.persistence

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.testing.application.SnippetTestPersistenceException
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestRepository
import age.of.printscript.demo.testing.domain.SnippetTestSummary
import age.of.printscript.demo.testing.infrastructure.persistence.mapper.toDomain
import age.of.printscript.demo.testing.infrastructure.persistence.mapper.toEntity
import age.of.printscript.demo.testing.infrastructure.persistence.mapper.toSummary
import age.of.printscript.demo.testing.infrastructure.persistence.repository.SnippetTestJpaRepository
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Repository
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

    override fun save(test: SnippetTest): SnippetTestSummary =
        try {
            jpaRepository
                .save(test.toEntity())
                .toSummary()
        } catch (failure: DataAccessException) {
            throw SnippetTestPersistenceException(failure)
        }
}
