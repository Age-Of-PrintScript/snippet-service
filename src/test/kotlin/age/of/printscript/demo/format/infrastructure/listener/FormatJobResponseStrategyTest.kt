package age.of.printscript.demo.format.infrastructure.listener

import age.of.printscript.demo.format.application.CompleteFormatJob
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.jobs.FormatJobResponse
import age.of.printscript.jobs.LintJobResponse
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions

class FormatJobResponseStrategyTest {
    private val completeFormatJob: CompleteFormatJob = mock(CompleteFormatJob::class.java)
    private val strategy = FormatJobResponseStrategy(completeFormatJob)

    @Test
    fun `supports returns true for FormatJobResponse and false for others`() {
        val formatResponse = FormatJobResponse("job-1")
        val lintResponse = LintJobResponse("job-1", emptyList())

        assertTrue(strategy.supports(formatResponse))
        assertFalse(strategy.supports(lintResponse))
    }

    @Test
    fun `handle delegates to CompleteFormatJob`() {
        val response = FormatJobResponse("job-123")

        strategy.handle(response)

        verify(completeFormatJob).invoke(JobId("job-123"))
    }

    @Test
    fun `handle does nothing when response is not FormatJobResponse`() {
        val lintResponse = LintJobResponse("job-123", emptyList())

        strategy.handle(lintResponse)

        verifyNoInteractions(completeFormatJob)
    }
}
