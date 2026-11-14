package age.of.printscript.demo.snippet.infrastructure.persistence

import age.of.printscript.demo.snippet.application.SnippetTestPersistenceException
import age.of.printscript.demo.snippet.application.port.SnippetTestRepository
import age.of.printscript.demo.snippet.domain.SnippetTest
import age.of.printscript.demo.snippet.domain.SnippetTestSummary
import age.of.printscript.demo.snippet.infrastructure.persistence.mapper.toEntity
import age.of.printscript.demo.snippet.infrastructure.persistence.mapper.toSummary
import age.of.printscript.demo.snippet.infrastructure.persistence.repository.SnippetTestJpaRepository
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Repository

@Repository
class PostgresSnippetTestRepository(
    private val jpaRepository: SnippetTestJpaRepository,
) : SnippetTestRepository {
    override fun save(
        test: SnippetTest,
        contentUrl: String,
    ): SnippetTestSummary =
        try {
            jpaRepository
                .save(test.toEntity(contentUrl))
                .toSummary()
        } catch (failure: DataAccessException) {
            throw SnippetTestPersistenceException(failure)
        }
}
