package age.of.printscript.demo.infrastructure.controller

import age.of.printscript.demo.application.CreateSnippet
import age.of.printscript.demo.application.CreateSnippetDto
import age.of.printscript.demo.application.CreateSnippetResult
import age.of.printscript.demo.application.GetSnippet
import age.of.printscript.demo.application.GetSnippetResult
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.SnippetWithContent
import age.of.printscript.demo.domain.UserId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class CreateSnippetRequest(
    val title: String,
    val description: String?,
    val language: String,
    val languageVersion: String,
    val content: String,
)

data class SnippetResponse(
    val id: String,
    val version: Int,
    val title: String,
    val description: String?,
    val ownerId: String,
    val language: String,
    val languageVersion: String,
    val content: String,
)

private fun CreateSnippetRequest.toDto(ownerId: UserId) =
    CreateSnippetDto(
        title = title,
        description = description,
        ownerId = ownerId,
        language = language,
        languageVersion = languageVersion,
    )

private fun SnippetWithContent.toResponse() =
    SnippetResponse(
        id = id.value,
        version = version,
        title = title,
        description = description,
        ownerId = ownerId.value,
        language = language,
        languageVersion = languageVersion,
        content = content,
    )

private fun GetSnippetResult.toResponseEntity(): ResponseEntity<SnippetResponse> =
    when (this) {
        is GetSnippetResult.Success -> ResponseEntity.ok(snippet.toResponse())
        is GetSnippetResult.NotFound -> ResponseEntity.notFound().build()
        is GetSnippetResult.Forbidden -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
    }

private fun CreateSnippetResult.toResponseEntity(): ResponseEntity<Any> =
    when (this) {
        is CreateSnippetResult.Created -> ResponseEntity.status(HttpStatus.CREATED).body(snippet.toResponse())
        is CreateSnippetResult.InvalidSnippet -> ResponseEntity.badRequest().body(errors)
        is CreateSnippetResult.UnsupportedLanguage ->
            ResponseEntity.badRequest().body(mapOf("error" to "Unsupported language: $language"))
        is CreateSnippetResult.UnsupportedVersion ->
            ResponseEntity.badRequest().body(mapOf("error" to "Unsupported version: $version"))
    }

@RestController
@RequestMapping("/snippet")
class SnippetController(
    private val getSnippet: GetSnippet,
    private val createSnippet: CreateSnippet,
) {
    @GetMapping("/{id}")
    fun get(
        @PathVariable id: String,
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) version: Int?,
    ): ResponseEntity<SnippetResponse> = getSnippet(SnippetId(id), UserId(userId), version).toResponseEntity()

    @PostMapping
    fun create(
        @RequestHeader("X-User-Id") userId: String,
        @RequestBody request: CreateSnippetRequest,
    ): ResponseEntity<Any> = createSnippet(request.toDto(UserId(userId)), request.content).toResponseEntity()
}
