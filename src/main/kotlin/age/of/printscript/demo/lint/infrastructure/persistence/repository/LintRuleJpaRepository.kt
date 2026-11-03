package age.of.printscript.demo.lint.infrastructure.persistence.repository

import age.of.printscript.demo.lint.infrastructure.persistence.entity.ActiveLintRuleEntity
import age.of.printscript.demo.lint.infrastructure.persistence.entity.ActiveLintRuleEntityId
import age.of.printscript.demo.lint.infrastructure.persistence.entity.LintRuleEntity
import org.springframework.data.jpa.repository.JpaRepository

interface LintRuleJpaRepository : JpaRepository<LintRuleEntity, Int>

interface ActiveLintRuleJpaRepository : JpaRepository<ActiveLintRuleEntity, ActiveLintRuleEntityId> {
    fun findAllByUserId(userId: String): List<ActiveLintRuleEntity>

    fun deleteAllByUserId(userId: String)
}
