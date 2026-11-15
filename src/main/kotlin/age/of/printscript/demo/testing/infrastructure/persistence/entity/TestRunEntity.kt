package age.of.printscript.demo.testing.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "test_run")
class TestRunEntity(
    @Id
    val id: UUID,
    @Column(name = "snippet_id", nullable = false)
    val snippetId: UUID,
    @Column(name = "snippet_version", nullable = false)
    val snippetVersion: Int,
    @Column(name = "requested_by", nullable = false, length = 128)
    val requestedBy: String,
    @Column(nullable = false, length = 30)
    val status: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "started_at")
    val startedAt: Instant? = null,
    @Column(name = "finished_at")
    val finishedAt: Instant? = null,
)
