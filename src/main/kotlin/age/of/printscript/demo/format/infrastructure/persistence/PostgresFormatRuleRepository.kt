package age.of.printscript.demo.format.infrastructure.persistence

import age.of.printscript.demo.format.domain.ActiveFormatRule
import age.of.printscript.demo.format.domain.FormatRule
import age.of.printscript.demo.format.domain.FormatRuleRepository
import age.of.printscript.demo.format.infrastructure.persistence.mapper.toDomain
import age.of.printscript.demo.format.infrastructure.persistence.mapper.toEntity
import age.of.printscript.demo.format.infrastructure.persistence.repository.ActiveFormatRuleJpaRepository
import age.of.printscript.demo.format.infrastructure.persistence.repository.FormatRuleJpaRepository
import age.of.printscript.demo.snippet.domain.UserId
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class PostgresFormatRuleRepository(
    private val formatRuleJpaRepository: FormatRuleJpaRepository,
    private val activeFormatRuleJpaRepository: ActiveFormatRuleJpaRepository,
) : FormatRuleRepository {
    override fun findAllRules(): List<FormatRule> =
        formatRuleJpaRepository
            .findAll()
            .map { it.toDomain() }

    override fun findActiveByUser(userId: UserId): List<ActiveFormatRule> =
        activeFormatRuleJpaRepository
            .findAllByUserId(userId.value)
            .map { it.toDomain() }

    @Transactional
    override fun replaceActiveForUser(
        userId: UserId,
        activeRules: List<ActiveFormatRule>,
    ): List<ActiveFormatRule> {
        activeFormatRuleJpaRepository.deleteAllByUserId(userId.value)
        val entities = activeRules.map { it.toEntity() }
        return activeFormatRuleJpaRepository
            .saveAll(entities)
            .map { it.toDomain() }
    }
}
