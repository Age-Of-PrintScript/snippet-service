package age.of.printscript.demo.infrastructure.persistance.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "job")
class JobEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,
    @Column(name = "snippet_id", nullable = false)
    val snippetId: UUID,
    @Column(name = "snippet_version", nullable = false)
    val snippetVersion: Int,
    @Column(name = "user_id", nullable = false, length = 128)
    val userId: String,
    @Column(name = "type", nullable = false, length = 30)
    val type: String,
    @Column(name = "status", nullable = false, length = 30)
    val status: String,
    @Column(name = "result", columnDefinition = "TEXT")
    val result: String? = null,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
)
