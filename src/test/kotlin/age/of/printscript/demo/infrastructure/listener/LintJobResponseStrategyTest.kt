package age.of.printscript.demo.infrastructure.listener

import age.of.printscript.demo.application.CompleteLintJob
import age.of.printscript.demo.domain.JobId
import age.of.printscript.demo.domain.LintWarning
import age.of.printscript.jobs.FormatJobResponse
import age.of.printscript.jobs.LintJobResponse
import age.of.printscript.jobs.LintWarningDto
import age.of.printscript.jobs.PositionDto
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions

class LintJobResponseStrategyTest {
    private val completeLintJob: CompleteLintJob = mock(CompleteLintJob::class.java)
    private val strategy = LintJobResponseStrategy(completeLintJob)

    @Test
    fun `supports returns true for LintJobResponse and false for others`() {
        val lintResponse = LintJobResponse("job-1", emptyList())
        val formatResponse = FormatJobResponse("job-1")

        assertTrue(strategy.supports(lintResponse))
        assertFalse(strategy.supports(formatResponse))
    }

    @Test
    fun `handle maps warnings and delegates to CompleteLintJob`() {
        val warningDto = LintWarningDto("Variable must be camelCase", PositionDto(2, 4))
        val response = LintJobResponse("job-123", listOf(warningDto))

        strategy.handle(response)

        val expectedWarnings = listOf(LintWarning("Variable must be camelCase", 2, 4))
        verify(completeLintJob).invoke(JobId("job-123"), expectedWarnings)
    }

    @Test
    fun `handle does nothing when response is not LintJobResponse`() {
        val formatResponse = FormatJobResponse("job-123")

        strategy.handle(formatResponse)

        verifyNoInteractions(completeLintJob)
    }
}
