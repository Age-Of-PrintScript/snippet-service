package age.of.printscript.demo.infrastructure.persistance.repository

import age.of.printscript.demo.infrastructure.persistance.entity.ActiveFormatRuleEntity
import age.of.printscript.demo.infrastructure.persistance.entity.ActiveFormatRuleEntityId
import age.of.printscript.demo.infrastructure.persistance.entity.FormatRuleEntity
import org.springframework.data.jpa.repository.JpaRepository

interface FormatRuleJpaRepository : JpaRepository<FormatRuleEntity, Int>

interface ActiveFormatRuleJpaRepository : JpaRepository<ActiveFormatRuleEntity, ActiveFormatRuleEntityId> {
    fun findAllByUserId(userId: String): List<ActiveFormatRuleEntity>

    fun deleteAllByUserId(userId: String)
}
