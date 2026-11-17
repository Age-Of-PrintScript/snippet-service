package age.of.printscript.demo.testing.infrastructure.controller

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.application.CreateSnippetTest
import age.of.printscript.demo.testing.application.CreateSnippetTestDto
import age.of.printscript.demo.testing.application.CreateSnippetTestResult
import age.of.printscript.demo.testing.application.DeleteSnippetTest
import age.of.printscript.demo.testing.application.DeleteSnippetTestDto
import age.of.printscript.demo.testing.application.DeleteSnippetTestResult
import age.of.printscript.demo.testing.domain.SnippetTestId
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class CreateSnippetTestRequest(
    val inputs: List<String>,
    val outputs: List<String>,
    val id: String? = null,
)

data class SnippetTestResponse(
    val id: String,
    val snippetId: String,
    val createdAt: Instant,
)

private fun CreateSnippetTestRequest.toDto(
    snippetId: SnippetId,
    ownerId: UserId,
) = CreateSnippetTestDto(
    snippetId = snippetId,
    ownerId = ownerId,
    inputs = inputs,
    outputs = outputs,
    id = id?.let { SnippetTestId(it) },
)

private fun CreateSnippetTestResult.toResponseEntity(): ResponseEntity<SnippetTestResponse> =
    when (this) {
        is CreateSnippetTestResult.Created ->
            ResponseEntity.status(HttpStatus.CREATED).body(
                SnippetTestResponse(
                    id = test.id.value,
                    snippetId = test.snippetId.value,
                    createdAt = test.createdAt,
                ),
            )
        is CreateSnippetTestResult.NotFound -> ResponseEntity.notFound().build()
        is CreateSnippetTestResult.Forbidden -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
    }

@RestController
@RequestMapping("/snippet")
class TestController(
    private val createSnippetTest: CreateSnippetTest,
    private val deleteSnippetTest: DeleteSnippetTest,
) {
    private val log = LoggerFactory.getLogger(TestController::class.java)

    @PostMapping("/{id}/test")
    fun createTest(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CreateSnippetTestRequest,
    ): ResponseEntity<SnippetTestResponse> =
        when (val result = createSnippetTest(request.toDto(SnippetId(id), UserId(jwt.subject)))) {
            is CreateSnippetTestResult.Created -> {
                log.info("Saved test id={} for snippet id={}", result.test.id.value, id)
                result.toResponseEntity()
            }
            is CreateSnippetTestResult.NotFound -> {
                log.warn("Cannot save test for snippet id={}: not found", id)
                result.toResponseEntity()
            }
            is CreateSnippetTestResult.Forbidden -> {
                log.warn("Cannot save test for snippet id={}: access denied", id)
                result.toResponseEntity()
            }
        }

    @DeleteMapping("/test/{testId}")
    fun deleteTest(
        @PathVariable testId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Void> =
        when (deleteSnippetTest(DeleteSnippetTestDto(SnippetTestId(testId), UserId(jwt.subject)))) {
            is DeleteSnippetTestResult.Success -> {
                log.info("Deleted test id={}", testId)
                ResponseEntity.noContent().build()
            }
            is DeleteSnippetTestResult.NotFound -> {
                log.warn("Cannot delete test id={}: not found", testId)
                ResponseEntity.notFound().build()
            }
            is DeleteSnippetTestResult.Forbidden -> {
                log.warn("Cannot delete test id={}: access denied", testId)
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        }
}
