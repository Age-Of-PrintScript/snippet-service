package age.of.printscript.demo.lint.infrastructure.persistence.repository

import age.of.printscript.demo.lint.infrastructure.persistence.entity.SnippetLintStatusEntity
import age.of.printscript.demo.lint.infrastructure.persistence.entity.SnippetLintStatusEntityId
import org.springframework.data.jpa.repository.JpaRepository

interface SnippetLintStatusJpaRepository : JpaRepository<SnippetLintStatusEntity, SnippetLintStatusEntityId>
