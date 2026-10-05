package age.of.printscript.demo.infrastructure.persistance.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "snippet")
@IdClass(SnippetEntityId::class)
class SnippetEntity(
    @Id
    val id: UUID,

    @Id
    val version: Int = 1,

    @Column(name = "owner_id", nullable = false, length = 128)
    val ownerId: String,

    @Column(nullable = false, length = 255)
    val title: String,

    @Column(columnDefinition = "TEXT")
    val description: String?,

    @Column(nullable = false, length = 50)
    val language: String,

    @Column(name = "language_version", nullable = false, length = 50)
    val languageVersion: String,

    @Column(nullable = false, length = 1024)
    val url: String,

    @Column(name = "is_latest", nullable = false)
    val isLatest: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
)
