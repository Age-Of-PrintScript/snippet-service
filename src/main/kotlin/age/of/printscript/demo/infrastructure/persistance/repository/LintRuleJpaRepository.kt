package age.of.printscript.demo.infrastructure.persistance.repository

import age.of.printscript.demo.infrastructure.persistance.entity.ActiveLintRuleEntity
import age.of.printscript.demo.infrastructure.persistance.entity.ActiveLintRuleEntityId
import age.of.printscript.demo.infrastructure.persistance.entity.LintRuleEntity
import org.springframework.data.jpa.repository.JpaRepository

interface LintRuleJpaRepository : JpaRepository<LintRuleEntity, Int>

interface ActiveLintRuleJpaRepository : JpaRepository<ActiveLintRuleEntity, ActiveLintRuleEntityId> {
    fun findAllByUserId(userId: String): List<ActiveLintRuleEntity>

    fun deleteAllByUserId(userId: String)
}
