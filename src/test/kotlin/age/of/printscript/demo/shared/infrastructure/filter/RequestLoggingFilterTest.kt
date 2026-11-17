package age.of.printscript.demo.shared.infrastructure.filter

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class RequestLoggingFilterTest {
    private val filter = RequestLoggingFilter()

    @Test
    fun `uses existing correlation id from request header and sets response header`() {
        val request = MockHttpServletRequest()
        val customCorrelationId = "test-corr-456"
        request.addHeader(RequestLoggingFilter.CORRELATION_ID_HEADER, customCorrelationId)
        val response = MockHttpServletResponse()

        var capturedCorrelationId: String? = null
        var capturedService: String? = null

        val chain =
            FilterChain { _, _ ->
                capturedCorrelationId = MDC.get(RequestLoggingFilter.CORRELATION_ID_KEY)
                capturedService = MDC.get(RequestLoggingFilter.SERVICE_KEY)
            }

        filter.doFilter(request, response, chain)

        assertEquals(customCorrelationId, capturedCorrelationId)
        assertEquals(RequestLoggingFilter.SERVICE_NAME, capturedService)
        assertEquals(customCorrelationId, response.getHeader(RequestLoggingFilter.CORRELATION_ID_HEADER))
        assertNull(MDC.get(RequestLoggingFilter.CORRELATION_ID_KEY))
    }

    @Test
    fun `generates new correlation id when header is absent`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        var capturedCorrelationId: String? = null

        val chain =
            FilterChain { _, _ ->
                capturedCorrelationId = MDC.get(RequestLoggingFilter.CORRELATION_ID_KEY)
            }

        filter.doFilter(request, response, chain)

        assertNotNull(capturedCorrelationId)
        assertEquals(capturedCorrelationId, response.getHeader(RequestLoggingFilter.CORRELATION_ID_HEADER))
        assertNull(MDC.get(RequestLoggingFilter.CORRELATION_ID_KEY))
    }
}
