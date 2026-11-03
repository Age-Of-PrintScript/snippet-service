package age.of.printscript.demo.infrastructure.listener

import age.of.printscript.jobs.JobResponse
import org.springframework.stereotype.Component

interface JobResponseStrategy {
    fun supports(response: JobResponse): Boolean

    fun handle(response: JobResponse)
}

@Component
class JobResponseHandler(
    private val strategies: List<JobResponseStrategy>,
) {
    fun handle(response: JobResponse) {
        val strategy = strategies.find { it.supports(response) }
        strategy?.handle(response)
    }
}
