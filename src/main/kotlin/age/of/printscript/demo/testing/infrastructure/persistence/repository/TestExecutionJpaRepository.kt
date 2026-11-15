package age.of.printscript.demo.testing.infrastructure.persistence.repository

import age.of.printscript.demo.testing.infrastructure.persistence.entity.TestExecutionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface TestExecutionJpaRepository : JpaRepository<TestExecutionEntity, UUID> {
    fun findAllByRunIdOrderByCreatedAtAsc(runId: UUID): List<TestExecutionEntity>

    @Query(
        value = """SELECT e.* FROM test_execution e JOIN test_run r ON r.id = e.run_id
            WHERE e.test_id = :testId AND r.snippet_version = :version AND e.is_stale = FALSE
              AND e.status IN ('PASSED', 'FAILED', 'ERROR', 'TIMED_OUT')
            ORDER BY e.finished_at DESC NULLS LAST, e.created_at DESC LIMIT 1""",
        nativeQuery = true,
    )
    fun findLatestCompleted(
        @Param("testId") testId: UUID,
        @Param("version") version: Int,
    ): TestExecutionEntity?
}
