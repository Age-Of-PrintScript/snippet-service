package age.of.printscript.demo.infrastructure.persistance.repository

import age.of.printscript.demo.infrastructure.persistance.entity.SnippetLintStatusEntity
import age.of.printscript.demo.infrastructure.persistance.entity.SnippetLintStatusEntityId
import org.springframework.data.jpa.repository.JpaRepository

interface SnippetLintStatusJpaRepository : JpaRepository<SnippetLintStatusEntity, SnippetLintStatusEntityId>
