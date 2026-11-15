package age.of.printscript.demo.testing.infrastructure.persistence.repository

import age.of.printscript.demo.testing.infrastructure.persistence.entity.SnippetTestEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

interface SnippetTestJpaRepository : JpaRepository<SnippetTestEntity, UUID> {
    fun findAllBySnippetId(snippetId: UUID): List<SnippetTestEntity>

    @Modifying
    @Transactional
    @Query(
        "UPDATE SnippetTestEntity t SET t.status = 'PENDING', t.activeJobId = :jobId, " +
            "t.updatedAt = :updatedAt WHERE t.id = :id",
    )
    fun markPending(
        @Param("id") id: UUID,
        @Param("jobId") jobId: UUID,
        @Param("updatedAt") updatedAt: Instant,
    ): Int
}
