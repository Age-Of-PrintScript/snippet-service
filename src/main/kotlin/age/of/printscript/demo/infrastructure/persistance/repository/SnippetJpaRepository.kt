package age.of.printscript.demo.infrastructure.persistance.repository

import age.of.printscript.demo.infrastructure.persistance.entity.SnippetEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SnippetJpaRepository : JpaRepository<SnippetEntity, UUID> {
    fun findAllByOwnerId(ownerId: String): List<SnippetEntity>
}
