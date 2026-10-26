package age.of.printscript.demo.infrastructure.controller

import age.of.printscript.demo.application.LintSnippet
import age.of.printscript.demo.application.LintSnippetResult
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class LintJobAcceptedResponse(
    val jobId: String,
)

private fun LintSnippetResult.toResponseEntity(): ResponseEntity<LintJobAcceptedResponse> =
    when (this) {
        is LintSnippetResult.Accepted ->
            ResponseEntity.status(HttpStatus.ACCEPTED).body(LintJobAcceptedResponse(jobId))
        is LintSnippetResult.NotFound -> ResponseEntity.notFound().build()
        is LintSnippetResult.Forbidden -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
    }

@RestController
@RequestMapping("/lint")
class LintController(
    private val lintSnippet: LintSnippet,
) {
    @PostMapping("/{id}")
    fun lint(
        @PathVariable id: String,
        @RequestHeader("X-User-Id") userId: String,
    ): ResponseEntity<LintJobAcceptedResponse> =
        lintSnippet(
            SnippetId(id),
            UserId(userId),
        ).toResponseEntity()
}
