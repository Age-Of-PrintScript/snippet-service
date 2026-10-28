package age.of.printscript.demo.infrastructure.listener

import age.of.printscript.demo.application.CompleteLintJob
import age.of.printscript.demo.domain.JobId
import age.of.printscript.demo.domain.LintWarning
import age.of.printscript.jobs.JobResponse
import age.of.printscript.jobs.LintJobResponse
import org.springframework.stereotype.Component

@Component
class LintJobResponseStrategy(
    private val completeLintJob: CompleteLintJob,
) : JobResponseStrategy {
    override fun supports(response: JobResponse): Boolean = response is LintJobResponse

    override fun handle(response: JobResponse) {
        if (response !is LintJobResponse) return
        val warnings =
            response.warnings.map { warning ->
                LintWarning(
                    message = warning.message,
                    line = warning.positionDto.line,
                    column = warning.positionDto.charPosition,
                )
            }
        completeLintJob(JobId(response.jobId), warnings)
    }
}
