package age.of.printscript.demo.testing.infrastructure.controller

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.application.CreateSnippetTest
import age.of.printscript.demo.testing.application.CreateSnippetTestDto
import age.of.printscript.demo.testing.application.CreateSnippetTestResult
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class CreateSnippetTestRequest(
    val inputs: List<String>,
    val outputs: List<String>,
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
) {
    @PostMapping("/{id}/test")
    fun createTest(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CreateSnippetTestRequest,
    ): ResponseEntity<SnippetTestResponse> =
        createSnippetTest(
            request.toDto(SnippetId(id), UserId(jwt.subject)),
        ).toResponseEntity()
}
