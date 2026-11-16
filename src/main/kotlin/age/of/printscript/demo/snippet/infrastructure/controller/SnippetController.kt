package age.of.printscript.demo.snippet.infrastructure.controller

import age.of.printscript.demo.snippet.application.CreateSnippet
import age.of.printscript.demo.snippet.application.CreateSnippetDto
import age.of.printscript.demo.snippet.application.CreateSnippetResult
import age.of.printscript.demo.snippet.application.GetSnippet
import age.of.printscript.demo.snippet.application.GetSnippetResult
import age.of.printscript.demo.snippet.application.UpdateSnippet
import age.of.printscript.demo.snippet.application.UpdateSnippetResult
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetWithContent
import age.of.printscript.demo.snippet.domain.UserId
import org.slf4j.LoggerFactory
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
    private val createSnippet: CreateSnippet,
    private val updateSnippet: UpdateSnippet,
) {
    private val log = LoggerFactory.getLogger(SnippetController::class.java)

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(required = false) version: Int?,
    ): ResponseEntity<SnippetResponse> =
        when (val result = getSnippet(SnippetId(id), UserId(jwt.subject), version)) {
            is GetSnippetResult.Success -> {
                log.info("Fetched snippet id={} version={}", id, result.snippet.version)
                result.toResponseEntity()
            }
            is GetSnippetResult.NotFound -> {
                log.warn("Snippet id={} not found", id)
                result.toResponseEntity()
            }
            is GetSnippetResult.Forbidden -> {
                log.warn("Access denied for snippet id={}", id)
                result.toResponseEntity()
            }
        }

    @PostMapping
    fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CreateSnippetRequest,
    ): ResponseEntity<Any> =
        when (val result = createSnippet(request.toDto(UserId(jwt.subject)), request.content)) {
            is CreateSnippetResult.Created -> {
                log.info(
                    "Created snippet id={} title='{}' language={}",
                    result.snippet.id.value,
                    request.title,
                    request.language,
                )
                result.toResponseEntity()
            }
            is CreateSnippetResult.InvalidSnippet -> {
                log.warn("Failed to create snippet: invalid syntax/errors={}", result.errors)
                result.toResponseEntity()
            }
            is CreateSnippetResult.UnsupportedLanguage -> {
                log.warn("Failed to create snippet: unsupported language={}", result.language)
                result.toResponseEntity()
            }
            is CreateSnippetResult.UnsupportedVersion -> {
                log.warn("Failed to create snippet: unsupported version={}", result.version)
                result.toResponseEntity()
            }
        }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: UpdateSnippetRequest,
    ): ResponseEntity<Any> =
        when (val result = updateSnippet(SnippetId(id), UserId(jwt.subject), request.content)) {
            is UpdateSnippetResult.Updated -> {
                log.info("Updated snippet id={} newVersion={}", id, result.snippet.version)
                result.toResponseEntity()
            }
            is UpdateSnippetResult.NotFound -> {
                log.warn("Cannot update snippet id={}: not found", id)
                result.toResponseEntity()
            }
            is UpdateSnippetResult.Forbidden -> {
                log.warn("Cannot update snippet id={}: access denied", id)
                result.toResponseEntity()
            }
            is UpdateSnippetResult.InvalidSnippet -> {
                log.warn("Cannot update snippet id={}: invalid snippet with errors={}", id, result.errors)
                result.toResponseEntity()
            }
            is UpdateSnippetResult.UnsupportedLanguage -> {
                log.warn("Cannot update snippet id={}: unsupported language={}", id, result.language)
                result.toResponseEntity()
            }
            is UpdateSnippetResult.UnsupportedVersion -> {
                log.warn("Cannot update snippet id={}: unsupported version={}", id, result.version)
                result.toResponseEntity()
            }
        }
}
