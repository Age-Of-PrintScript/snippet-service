package age.of.printscript.demo.testing.infrastructure.controller

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.application.GetTestRun
import age.of.printscript.demo.testing.application.GetTestRunResult
import age.of.printscript.demo.testing.application.ListSnippetTests
import age.of.printscript.demo.testing.application.ListSnippetTestsResult
import age.of.printscript.demo.testing.application.StartTestRun
import age.of.printscript.demo.testing.application.StartTestRunResult
import age.of.printscript.demo.testing.application.TestSelection
import age.of.printscript.demo.testing.application.TestSelectionType
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.TestExecution
import age.of.printscript.demo.testing.domain.TestRunId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class LastExecutionResponse(
    val status: String,
    val finishedAt: Instant?,
)

data class TestListItemResponse(
    val id: String,
    val snippetId: String,
    val createdAt: Instant,
    val inputs: List<String>,
    val outputs: List<String>,
    val lastExecution: LastExecutionResponse?,
)

@RestController
@RequestMapping("/tests")
class TestsQueryController(
    private val listSnippetTests: ListSnippetTests,
) {
    @GetMapping
    fun list(
        @RequestParam snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Any> =
        when (val result = listSnippetTests(SnippetId(snippetId), UserId(jwt.subject))) {
            is ListSnippetTestsResult.Success ->
                ResponseEntity.ok(
                    result.tests.map { test ->
                        TestListItemResponse(
                            test.id.value,
                            test.snippetId.value,
                            test.createdAt,
                            test.content.inputs,
                            test.content.outputs,
                            test.lastExecution?.let { LastExecutionResponse(it.status.name, it.finishedAt) },
                        )
                    },
                )
            ListSnippetTestsResult.NotFound -> ResponseEntity.notFound().build()
            ListSnippetTestsResult.Forbidden -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
}

data class TestSelectionRequest(
    val type: TestSelectionType,
    val testIds: List<String> = emptyList(),
)

data class StartTestRunRequest(
    val snippetId: String,
    val selection: TestSelectionRequest,
)

data class TestRunAcceptedResponse(
    val runId: String,
    val status: String,
)

data class TestExecutionResponse(
    val testId: String,
    val status: String,
)

data class TestRunResponse(
    val status: String,
    val tests: List<TestExecutionResponse>,
)

@RestController
@RequestMapping("/test-runs")
class TestRunController(
    private val startTestRun: StartTestRun,
    private val getTestRun: GetTestRun,
) {
    @PostMapping
    fun start(
        @RequestBody request: StartTestRunRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Any> =
        when (
            val result =
                startTestRun(
                    SnippetId(request.snippetId),
                    UserId(jwt.subject),
                    TestSelection(request.selection.type, request.selection.testIds.map(::SnippetTestId)),
                )
        ) {
            is StartTestRunResult.Accepted ->
                ResponseEntity
                    .status(
                        HttpStatus.ACCEPTED,
                    ).body(TestRunAcceptedResponse(result.run.id.value, result.run.status.name))
            StartTestRunResult.NotFound -> ResponseEntity.notFound().build()
            StartTestRunResult.Forbidden -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            StartTestRunResult.InvalidSelection ->
                ResponseEntity.badRequest().body(
                    mapOf("error" to "Invalid test selection"),
                )
        }

    @GetMapping("/{runId}")
    fun get(
        @PathVariable runId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Any> =
        when (val result = getTestRun(TestRunId(runId), UserId(jwt.subject))) {
            is GetTestRunResult.Success ->
                ResponseEntity.ok(
                    TestRunResponse(result.run.status.name, result.tests.map(TestExecution::toResponse)),
                )
            GetTestRunResult.NotFound -> ResponseEntity.notFound().build()
            GetTestRunResult.Forbidden -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
}

private fun TestExecution.toResponse(): TestExecutionResponse = TestExecutionResponse(testId.value, status.name)
