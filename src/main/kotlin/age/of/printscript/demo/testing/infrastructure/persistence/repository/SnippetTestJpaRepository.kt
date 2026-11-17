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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
        "UPDATE SnippetTestEntity t SET t.status = :status, t.activeJobId = NULL, t.updatedAt = :updatedAt " +
            "WHERE t.activeJobId = :jobId AND t.status = 'PENDING'",
    )
    fun completeIfActive(
        @Param("jobId") jobId: UUID,
        @Param("status") status: String,
        @Param("updatedAt") updatedAt: Instant,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
        "UPDATE SnippetTestEntity t SET t.status = 'NOT_RUN', t.activeJobId = NULL, t.updatedAt = :updatedAt " +
            "WHERE t.snippetId = :snippetId",
    )
    fun invalidateForSnippet(
        @Param("snippetId") snippetId: UUID,
        @Param("updatedAt") updatedAt: Instant,
    ): Int
}
