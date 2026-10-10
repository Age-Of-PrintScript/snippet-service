package age.of.printscript.demo.infrastructure.persistance.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "snippet_test")
class SnippetTestEntity(
    @Id
    val id: UUID,
    @Column(name = "snippet_id", nullable = false)
    val snippetId: UUID,
    @Column(name = "owner_id", nullable = false, length = 128)
    val ownerId: String,
    @Column(nullable = false, length = 1024)
    val url: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
)
