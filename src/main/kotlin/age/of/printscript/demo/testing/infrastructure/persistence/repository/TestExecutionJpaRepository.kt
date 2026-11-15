package age.of.printscript.demo.testing.infrastructure.persistence.repository

import age.of.printscript.demo.testing.infrastructure.persistence.entity.TestExecutionEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TestExecutionJpaRepository : JpaRepository<TestExecutionEntity, UUID> {
    fun findAllByRunIdOrderByCreatedAtAsc(runId: UUID): List<TestExecutionEntity>
}
