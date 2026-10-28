package age.of.printscript.demo.infrastructure.listener

import age.of.printscript.jobs.FormatJobResponse
import age.of.printscript.jobs.LintJobResponse
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class JobResponseHandlerTest {
    private val strategy1: JobResponseStrategy = mock(JobResponseStrategy::class.java)
    private val strategy2: JobResponseStrategy = mock(JobResponseStrategy::class.java)
    private val handler = JobResponseHandler(listOf(strategy1, strategy2))

    @Test
    fun `delegates to matching strategy when one supports the response`() {
        val response = LintJobResponse("job-1", emptyList())
        `when`(strategy1.supports(response)).thenReturn(false)
        `when`(strategy2.supports(response)).thenReturn(true)

        handler.handle(response)

        verify(strategy1, never()).handle(response)
        verify(strategy2).handle(response)
    }

    @Test
    fun `does nothing when no strategy supports the response`() {
        val response = FormatJobResponse("job-1")
        `when`(strategy1.supports(response)).thenReturn(false)
        `when`(strategy2.supports(response)).thenReturn(false)

        handler.handle(response)

        verify(strategy1, never()).handle(response)
        verify(strategy2, never()).handle(response)
    }
}
