package age.of.printscript.demo.snippet.infrastructure.persistence.repository

import age.of.printscript.demo.snippet.infrastructure.persistence.entity.SnippetEntity
import age.of.printscript.demo.snippet.infrastructure.persistence.entity.SnippetEntityId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface SnippetJpaRepository : JpaRepository<SnippetEntity, SnippetEntityId> {
    fun findByIdAndIsLatestTrue(id: UUID): SnippetEntity?

    fun findAllByOwnerIdAndIsLatestTrue(ownerId: String): List<SnippetEntity>

    fun findAllByIdOrderByVersionDesc(id: UUID): List<SnippetEntity>

    @Query(
        """
        SELECT s FROM SnippetEntity s
        WHERE s.id IN :ids
          AND s.isLatest = true
          AND (:name IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :name, '%')))
        """,
    )
    fun findLatestByIds(
        @Param("ids") ids: Collection<UUID>,
        @Param("name") name: String?,
        pageable: Pageable,
    ): Page<SnippetEntity>

    @Modifying
    @Query(
        "UPDATE SnippetEntity s SET s.isLatest = false " +
            "WHERE s.id = :id AND s.version <> :version AND s.isLatest = true",
    )
    fun clearPreviousLatest(
        @Param("id") id: UUID,
        @Param("version") version: Int,
    ): Int
}
