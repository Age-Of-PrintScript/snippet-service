package age.of.printscript.demo.jobs.infrastructure.controller

import age.of.printscript.demo.jobs.application.GetJobStatus
import age.of.printscript.demo.jobs.application.GetJobStatusResult
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.snippet.domain.UserId
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class JobStatusResponse(
    val id: String,
    val status: String,
)

@RestController
@RequestMapping("/jobs")
class JobsController(
    private val getJobStatus: GetJobStatus,
) {
    private val log = LoggerFactory.getLogger(JobsController::class.java)

    @GetMapping("/{id}")
    fun status(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Any> =
        when (val result = getJobStatus(JobId(id), UserId(jwt.subject))) {
            is GetJobStatusResult.Success -> {
                log.info("Fetched status for job id={}: {}", id, result.status)
                ResponseEntity.ok(JobStatusResponse(id, result.status.name))
            }
            GetJobStatusResult.NotFound -> {
                log.warn("Cannot fetch status for job id={}: not found", id)
                ResponseEntity.notFound().build()
            }
            GetJobStatusResult.Forbidden -> {
                log.warn("Cannot fetch status for job id={}: access denied", id)
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        }
}
