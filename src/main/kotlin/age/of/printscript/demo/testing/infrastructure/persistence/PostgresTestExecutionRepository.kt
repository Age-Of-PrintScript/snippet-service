package age.of.printscript.demo.testing.infrastructure.persistence

import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.TestExecution
import age.of.printscript.demo.testing.domain.TestExecutionId
import age.of.printscript.demo.testing.domain.TestExecutionRepository
import age.of.printscript.demo.testing.domain.TestRunId
import age.of.printscript.demo.testing.infrastructure.persistence.mapper.toDomain
import age.of.printscript.demo.testing.infrastructure.persistence.mapper.toEntity
import age.of.printscript.demo.testing.infrastructure.persistence.repository.TestExecutionJpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class PostgresTestExecutionRepository(
    private val jpaRepository: TestExecutionJpaRepository,
) : TestExecutionRepository {
    override fun findById(id: TestExecutionId): TestExecution? =
        jpaRepository
            .findById(UUID.fromString(id.value))
            .orElse(null)
            ?.toDomain()

    override fun findAllByRunId(runId: TestRunId): List<TestExecution> =
        jpaRepository
            .findAllByRunIdOrderByCreatedAtAsc(UUID.fromString(runId.value))
            .map { it.toDomain() }

    override fun findLatestCompleted(
        testId: SnippetTestId,
        snippetVersion: Int,
    ): TestExecution? =
        jpaRepository
            .findLatestCompleted(
                UUID.fromString(testId.value),
                snippetVersion,
            )?.toDomain()

    override fun save(testExecution: TestExecution): TestExecution =
        jpaRepository
            .save(
                testExecution.toEntity(),
            ).toDomain()
}
