package age.of.printscript.demo.testing.infrastructure.persistence

import age.of.printscript.demo.testing.application.SnippetTestPersistenceException
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestRepository
import age.of.printscript.demo.testing.domain.SnippetTestSummary
import age.of.printscript.demo.testing.infrastructure.persistence.mapper.toEntity
import age.of.printscript.demo.testing.infrastructure.persistence.mapper.toSummary
import age.of.printscript.demo.testing.infrastructure.persistence.repository.SnippetTestJpaRepository
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Repository

@Repository
class PostgresSnippetTestRepository(
    private val jpaRepository: SnippetTestJpaRepository,
) : SnippetTestRepository {
    override fun save(test: SnippetTest): SnippetTestSummary =
        try {
            jpaRepository
                .save(test.toEntity())
                .toSummary()
        } catch (failure: DataAccessException) {
            throw SnippetTestPersistenceException(failure)
        }
}
