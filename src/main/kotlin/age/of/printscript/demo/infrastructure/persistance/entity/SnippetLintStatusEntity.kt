package age.of.printscript.demo.infrastructure.persistance.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.time.Instant
import java.util.UUID

data class SnippetLintStatusEntityId(
    val snippetId: UUID = UUID.randomUUID(),
    val version: Int = 1,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

@Entity
@Table(name = "snippet_lint_status")
@IdClass(SnippetLintStatusEntityId::class)
class SnippetLintStatusEntity(
    @Id
    @Column(name = "snippet_id", nullable = false)
    val snippetId: UUID,
    @Id
    @Column(name = "version", nullable = false)
    val version: Int = 1,
    @Column(name = "status", nullable = false, length = 30)
    val status: String,
    @Column(name = "warnings", columnDefinition = "TEXT")
    val warnings: String? = null,
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
)
