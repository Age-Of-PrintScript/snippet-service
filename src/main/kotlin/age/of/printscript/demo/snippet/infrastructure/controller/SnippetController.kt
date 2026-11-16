package age.of.printscript.demo.snippet.infrastructure.controller

import age.of.printscript.demo.snippet.application.CreateSnippet
import age.of.printscript.demo.snippet.application.CreateSnippetDto
import age.of.printscript.demo.snippet.application.CreateSnippetResult
import age.of.printscript.demo.snippet.application.GetSnippet
import age.of.printscript.demo.snippet.application.GetSnippetResult
import age.of.printscript.demo.snippet.application.ListSnippets
import age.of.printscript.demo.snippet.application.ListSnippetsResult
import age.of.printscript.demo.snippet.application.SnippetSummary
import age.of.printscript.demo.snippet.application.UpdateSnippet
import age.of.printscript.demo.snippet.application.UpdateSnippetResult
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetWithContent
import age.of.printscript.demo.snippet.domain.UserId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
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

data class UpdateSnippetRequest(
    val content: String,
)

data class SnippetSummaryResponse(
    val snippetId: String,
    val title: String,
    val compliance: String,
    val author: String,
    val language: String,
    val languageVersion: String,
    val role: String,
)

data class PaginatedSnippetsResponse(
    val page: Int,
    val pageSize: Int,
    val total: Long,
    val snippets: List<SnippetSummaryResponse>,
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

private fun SnippetSummary.toResponse() =
    SnippetSummaryResponse(
        snippetId = snippetId.value,
        title = title,
        compliance = compliance.name,
        author = author.value,
        language = language,
        languageVersion = languageVersion,
        role = role,
    )

private fun ListSnippetsResult.toResponse() =
    PaginatedSnippetsResponse(
        page = page,
        pageSize = pageSize,
        total = total,
        snippets = snippets.map { it.toResponse() },
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

private fun UpdateSnippetResult.toResponseEntity(): ResponseEntity<Any> =
    when (this) {
        is UpdateSnippetResult.Updated -> ResponseEntity.ok(snippet.toResponse())
        is UpdateSnippetResult.NotFound -> ResponseEntity.notFound().build()
        is UpdateSnippetResult.Forbidden -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        is UpdateSnippetResult.InvalidSnippet -> ResponseEntity.badRequest().body(errors)
        is UpdateSnippetResult.UnsupportedLanguage ->
            ResponseEntity.badRequest().body(mapOf("error" to "Unsupported language: $language"))
        is UpdateSnippetResult.UnsupportedVersion ->
            ResponseEntity.badRequest().body(mapOf("error" to "Unsupported version: $version"))
    }

@RestController
@RequestMapping("/snippet")
class SnippetController(
    private val getSnippet: GetSnippet,
    private val listSnippets: ListSnippets,
    private val createSnippet: CreateSnippet,
    private val updateSnippet: UpdateSnippet,
) {
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(required = false) name: String?,
    ): ResponseEntity<PaginatedSnippetsResponse> =
        ResponseEntity.ok(
            listSnippets(page = page, pageSize = pageSize, name = name).toResponse(),
        )

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(required = false) version: Int?,
    ): ResponseEntity<SnippetResponse> =
        getSnippet(
            SnippetId(id),
            UserId(jwt.subject),
            version,
        ).toResponseEntity()

    @PostMapping
    fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CreateSnippetRequest,
    ): ResponseEntity<Any> =
        createSnippet(
            request.toDto(UserId(jwt.subject)),
            request.content,
        ).toResponseEntity()

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: UpdateSnippetRequest,
    ): ResponseEntity<Any> =
        updateSnippet(
            SnippetId(id),
            UserId(jwt.subject),
            request.content,
        ).toResponseEntity()
}
