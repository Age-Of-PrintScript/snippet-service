package age.of.printscript.demo.testing.infrastructure.persistence

import age.of.printscript.demo.testing.domain.TestRun
import age.of.printscript.demo.testing.domain.TestRunId
import age.of.printscript.demo.testing.domain.TestRunRepository
import age.of.printscript.demo.testing.infrastructure.persistence.mapper.toDomain
import age.of.printscript.demo.testing.infrastructure.persistence.mapper.toEntity
import age.of.printscript.demo.testing.infrastructure.persistence.repository.TestRunJpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class PostgresTestRunRepository(
    private val jpaRepository: TestRunJpaRepository,
) : TestRunRepository {
    override fun findById(id: TestRunId): TestRun? =
        jpaRepository
            .findById(UUID.fromString(id.value))
            .orElse(null)
            ?.toDomain()

    override fun save(testRun: TestRun): TestRun = jpaRepository.save(testRun.toEntity()).toDomain()
}
