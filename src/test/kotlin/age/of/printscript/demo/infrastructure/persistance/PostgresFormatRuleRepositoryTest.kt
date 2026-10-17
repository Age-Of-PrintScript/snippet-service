package age.of.printscript.demo.infrastructure.persistance

import age.of.printscript.demo.domain.ActiveFormatRule
import age.of.printscript.demo.domain.FormatRuleId
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.infrastructure.persistance.entity.ActiveFormatRuleEntity
import age.of.printscript.demo.infrastructure.persistance.entity.FormatRuleEntity
import age.of.printscript.demo.infrastructure.persistance.repository.ActiveFormatRuleJpaRepository
import age.of.printscript.demo.infrastructure.persistance.repository.FormatRuleJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class PostgresFormatRuleRepositoryTest {
    private val formatRuleJpaRepository: FormatRuleJpaRepository = mock(FormatRuleJpaRepository::class.java)
    private val activeFormatRuleJpaRepository: ActiveFormatRuleJpaRepository =
        mock(ActiveFormatRuleJpaRepository::class.java)
    private val repository = PostgresFormatRuleRepository(formatRuleJpaRepository, activeFormatRuleJpaRepository)

    @Test
    fun `findAllRules returns mapped catalog format rules`() {
        val entity = FormatRuleEntity(id = 9, formatRule = "indent-inside-if", defaultValue = 4)
        `when`(formatRuleJpaRepository.findAll()).thenReturn(listOf(entity))

        val result = repository.findAllRules()

        assertEquals(1, result.size)
        assertEquals(FormatRuleId(9), result.first().id)
        assertEquals("indent-inside-if", result.first().name)
        assertEquals(4, result.first().defaultValue)
    }

    @Test
    fun `findActiveByUser returns mapped active format rules for user`() {
        val entity = ActiveFormatRuleEntity(userId = "alice", formatRuleId = 9, value = 2)
        `when`(activeFormatRuleJpaRepository.findAllByUserId("alice")).thenReturn(listOf(entity))

        val result = repository.findActiveByUser(UserId("alice"))

        assertEquals(1, result.size)
        assertEquals(UserId("alice"), result.first().userId)
        assertEquals(FormatRuleId(9), result.first().ruleId)
        assertEquals(2, result.first().value)
    }

    @Test
    fun `replaceActiveForUser deletes previous rules and saves new active rules`() {
        val activeRule = ActiveFormatRule(userId = UserId("alice"), ruleId = FormatRuleId(1), value = null)
        val savedEntity = ActiveFormatRuleEntity(userId = "alice", formatRuleId = 1, value = null)
        doReturn(listOf(savedEntity)).`when`(activeFormatRuleJpaRepository).saveAll(anyList())

        val result = repository.replaceActiveForUser(UserId("alice"), listOf(activeRule))

        verify(activeFormatRuleJpaRepository).deleteAllByUserId("alice")
        assertEquals(1, result.size)
        assertEquals(FormatRuleId(1), result.first().ruleId)
    }
}
