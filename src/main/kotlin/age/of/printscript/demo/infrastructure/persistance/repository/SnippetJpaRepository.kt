package age.of.printscript.demo.infrastructure.persistance.repository

import age.of.printscript.demo.infrastructure.persistance.entity.SnippetEntity
import age.of.printscript.demo.infrastructure.persistance.entity.SnippetEntityId
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SnippetJpaRepository : JpaRepository<SnippetEntity, SnippetEntityId> {
    fun findByIdAndIsLatestTrue(id: UUID): SnippetEntity?

    fun findAllByOwnerIdAndIsLatestTrue(ownerId: String): List<SnippetEntity>

    fun findAllByIdOrderByVersionDesc(id: UUID): List<SnippetEntity>
}
