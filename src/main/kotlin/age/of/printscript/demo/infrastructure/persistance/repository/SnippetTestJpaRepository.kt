package age.of.printscript.demo.infrastructure.persistance.repository

import age.of.printscript.demo.infrastructure.persistance.entity.SnippetTestEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SnippetTestJpaRepository : JpaRepository<SnippetTestEntity, UUID> {
    fun findAllBySnippetId(snippetId: UUID): List<SnippetTestEntity>
}
