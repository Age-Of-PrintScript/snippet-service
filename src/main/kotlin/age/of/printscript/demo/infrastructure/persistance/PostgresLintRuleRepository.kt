package age.of.printscript.demo.infrastructure.persistance

import age.of.printscript.demo.domain.ActiveLintRule
import age.of.printscript.demo.domain.LintRule
import age.of.printscript.demo.domain.LintRuleRepository
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.infrastructure.persistance.mapper.toDomain
import age.of.printscript.demo.infrastructure.persistance.mapper.toEntity
import age.of.printscript.demo.infrastructure.persistance.repository.ActiveLintRuleJpaRepository
import age.of.printscript.demo.infrastructure.persistance.repository.LintRuleJpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class PostgresLintRuleRepository(
    private val lintRuleJpaRepository: LintRuleJpaRepository,
    private val activeLintRuleJpaRepository: ActiveLintRuleJpaRepository,
) : LintRuleRepository {
    override fun findAllRules(): List<LintRule> =
        lintRuleJpaRepository
            .findAll()
            .map { it.toDomain() }

    override fun findActiveByUser(userId: UserId): List<ActiveLintRule> =
        activeLintRuleJpaRepository
            .findAllByUserId(userId.value)
            .map { it.toDomain() }

    @Transactional
    override fun replaceActiveForUser(
        userId: UserId,
        activeRules: List<ActiveLintRule>,
    ): List<ActiveLintRule> {
        activeLintRuleJpaRepository.deleteAllByUserId(userId.value)
        val entities = activeRules.map { it.toEntity() }
        return activeLintRuleJpaRepository
            .saveAll(entities)
            .map { it.toDomain() }
    }
}
