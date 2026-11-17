package age.of.printscript.demo.testing.infrastructure.controller

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.application.ListSnippetTestStatuses
import age.of.printscript.demo.testing.application.ListSnippetTestStatusesResult
import age.of.printscript.demo.testing.application.ListSnippetTests
import age.of.printscript.demo.testing.application.ListSnippetTestsResult
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class TestListItemResponse(
    val id: String,
    val snippetId: String,
    val createdAt: Instant,
    val inputs: List<String>,
    val outputs: List<String>,
    val status: String,
)

data class TestStatusResponse(
    val id: String,
    val status: String,
)

@RestController
@RequestMapping("/tests")
class TestsQueryController(
    private val listSnippetTests: ListSnippetTests,
    private val listSnippetTestStatuses: ListSnippetTestStatuses,
) {
    private val log = LoggerFactory.getLogger(TestsQueryController::class.java)

    @GetMapping("/statuses")
    fun statuses(
        @RequestParam snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Any> =
        when (val result = listSnippetTestStatuses(SnippetId(snippetId), UserId(jwt.subject))) {
            is ListSnippetTestStatusesResult.Success -> {
                log.info("Fetched {} test statuses for snippet id={}", result.tests.size, snippetId)
                ResponseEntity.ok(result.tests.map { TestStatusResponse(it.id.value, it.status.name) })
            }
            ListSnippetTestStatusesResult.NotFound -> {
                log.warn("Cannot fetch test statuses for snippet id={}: not found", snippetId)
                ResponseEntity.notFound().build()
            }
            ListSnippetTestStatusesResult.Forbidden -> {
                log.warn("Cannot fetch test statuses for snippet id={}: access denied", snippetId)
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        }

    @GetMapping
    fun list(
        @RequestParam snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Any> =
        when (val result = listSnippetTests(SnippetId(snippetId), UserId(jwt.subject))) {
            is ListSnippetTestsResult.Success -> {
                log.info("Listed {} tests for snippet id={}", result.tests.size, snippetId)
                ResponseEntity.ok(
                    result.tests.map { test ->
                        TestListItemResponse(
                            test.id.value,
                            test.snippetId.value,
                            test.createdAt,
                            test.content.inputs,
                            test.content.outputs,
                            test.status.name,
                        )
                    },
                )
            }
            ListSnippetTestsResult.NotFound -> {
                log.warn("Cannot list tests for snippet id={}: not found", snippetId)
                ResponseEntity.notFound().build()
            }
            ListSnippetTestsResult.Forbidden -> {
                log.warn("Cannot list tests for snippet id={}: access denied", snippetId)
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        }
}
