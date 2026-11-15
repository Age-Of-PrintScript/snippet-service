package age.of.printscript.demo.snippet.infrastructure.persistence.repository

import age.of.printscript.demo.snippet.infrastructure.persistence.entity.SnippetEntity
import age.of.printscript.demo.snippet.infrastructure.persistence.entity.SnippetEntityId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface SnippetJpaRepository : JpaRepository<SnippetEntity, SnippetEntityId> {
    fun findByIdAndIsLatestTrue(id: UUID): SnippetEntity?

    fun findAllByOwnerIdAndIsLatestTrue(ownerId: String): List<SnippetEntity>

    fun findAllByIdOrderByVersionDesc(id: UUID): List<SnippetEntity>

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
