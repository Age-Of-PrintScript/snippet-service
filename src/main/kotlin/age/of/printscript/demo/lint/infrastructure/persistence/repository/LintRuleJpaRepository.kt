package age.of.printscript.demo.lint.infrastructure.persistence.repository

import age.of.printscript.demo.lint.infrastructure.persistence.entity.ActiveLintRuleEntity
import age.of.printscript.demo.lint.infrastructure.persistence.entity.ActiveLintRuleEntityId
import age.of.printscript.demo.lint.infrastructure.persistence.entity.LintRuleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface LintRuleJpaRepository : JpaRepository<LintRuleEntity, Int>

interface ActiveLintRuleJpaRepository : JpaRepository<ActiveLintRuleEntity, ActiveLintRuleEntityId> {
    fun findAllByUserId(userId: String): List<ActiveLintRuleEntity>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ActiveLintRuleEntity a WHERE a.userId = :userId")
    fun deleteAllByUserId(userId: String)
}
