package age.of.printscript.demo.testing.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "test_execution")
class TestExecutionEntity(
    @Id
    val id: UUID,
    @Column(name = "run_id", nullable = false)
    val runId: UUID,
    @Column(name = "test_id", nullable = false)
    val testId: UUID,
    @Column(name = "job_id", nullable = false, unique = true)
    val jobId: UUID,
    @Column(name = "test_url", nullable = false, length = 1024)
    val testUrl: String,
    @Column(nullable = false, length = 30)
    val status: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "started_at")
    val startedAt: Instant? = null,
    @Column(name = "finished_at")
    val finishedAt: Instant? = null,
)
