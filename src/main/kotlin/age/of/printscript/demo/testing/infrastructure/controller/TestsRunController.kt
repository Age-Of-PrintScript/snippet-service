package age.of.printscript.demo.testing.infrastructure.controller

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.application.RunSnippetTests
import age.of.printscript.demo.testing.application.RunSnippetTestsResult
import age.of.printscript.demo.testing.application.TestSelection
import age.of.printscript.demo.testing.application.TestSelectionType
import age.of.printscript.demo.testing.domain.SnippetTestId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class RunTestsRequest(
    val snippetId: UUID,
    val selection: TestSelectionRequest,
)

data class TestSelectionRequest(
    val type: TestSelectionType,
    val testIds: List<UUID> = emptyList(),
)

data class RunTestsResponse(
    val testIds: List<String>,
)

@RestController
@RequestMapping("/tests")
class TestsRunController(
    private val runTests: RunSnippetTests,
) {
    @PostMapping("/run")
    fun run(
        @RequestBody request: RunTestsRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<RunTestsResponse> =
        runTests(
            SnippetId(request.snippetId.toString()),
            UserId(jwt.subject),
            TestSelection(request.selection.type, request.selection.testIds.map { SnippetTestId(it.toString()) }),
        ).toResponseEntity()
}

private fun RunSnippetTestsResult.toResponseEntity(): ResponseEntity<RunTestsResponse> =
    when (this) {
        is RunSnippetTestsResult.Accepted ->
            ResponseEntity.status(HttpStatus.ACCEPTED).body(RunTestsResponse(testIds.map { it.value }))
        RunSnippetTestsResult.NotFound -> ResponseEntity.notFound().build()
        RunSnippetTestsResult.Forbidden -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        RunSnippetTestsResult.InvalidSelection -> ResponseEntity.badRequest().build()
    }
