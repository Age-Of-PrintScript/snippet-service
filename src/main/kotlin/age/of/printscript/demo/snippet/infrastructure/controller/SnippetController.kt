package age.of.printscript.demo.snippet.infrastructure.controller

import age.of.printscript.demo.lint.domain.LintStatus
import age.of.printscript.demo.lint.domain.LintWarning
import age.of.printscript.demo.snippet.application.CreateSnippet
import age.of.printscript.demo.snippet.application.CreateSnippetDto
import age.of.printscript.demo.snippet.application.CreateSnippetResult
import age.of.printscript.demo.snippet.application.DeleteSnippet
import age.of.printscript.demo.snippet.application.DeleteSnippetResult
import age.of.printscript.demo.snippet.application.GetSnippet
import age.of.printscript.demo.snippet.application.GetSnippetResult
import age.of.printscript.demo.snippet.application.ListSnippets
import age.of.printscript.demo.snippet.application.ListSnippetsResult
import age.of.printscript.demo.snippet.application.SnippetRelation
import age.of.printscript.demo.snippet.application.SnippetSortField
import age.of.printscript.demo.snippet.application.SnippetSummary
import age.of.printscript.demo.snippet.application.SortDirection
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
import org.springframework.web.bind.annotation.DeleteMapping
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

data class LintWarningResponse(
    val message: String,
    val line: Int,
    val column: Int,
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
    val compliance: String = LintStatus.PENDING.name,
    val lintWarnings: List<LintWarningResponse> = emptyList(),
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

private fun LintWarning.toResponse() =
    LintWarningResponse(
        message = message,
        line = line,
        column = column,
    )

private fun SnippetWithContent.toResponse(
    compliance: LintStatus = LintStatus.PENDING,
    lintWarnings: List<LintWarning> = emptyList(),
) = SnippetResponse(
    id = id.value,
    version = version,
    title = title,
    description = description,
    ownerId = ownerId.value,
    language = language,
    languageVersion = languageVersion,
    content = content,
    compliance = compliance.name,
    lintWarnings = lintWarnings.map { it.toResponse() },
)

private fun GetSnippetResult.toResponseEntity(): ResponseEntity<SnippetResponse> =
    when (this) {
        is GetSnippetResult.Success ->
            ResponseEntity.ok(snippet.toResponse(compliance = compliance, lintWarnings = lintWarnings))
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
    private val deleteSnippet: DeleteSnippet,
) {
    private val log = LoggerFactory.getLogger(SnippetController::class.java)

    @GetMapping
    @Suppress("LongParameterList")
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) relation: String?,
        @RequestParam(required = false) language: String?,
        @RequestParam(required = false) compliance: String?,
        @RequestParam(required = false) sortBy: String?,
        @RequestParam(defaultValue = "asc") sortDir: String,
    ): ResponseEntity<PaginatedSnippetsResponse> {
        val parsedRelation = parseEnumOrNull<SnippetRelation>(relation) ?: SnippetRelation.ALL
        val parsedCompliance = parseEnumOrNull<LintStatus>(compliance)
        val parsedSortBy = parseEnumOrNull<SnippetSortField>(sortBy)
        val parsedSortDir = parseEnumOrNull<SortDirection>(sortDir) ?: SortDirection.ASC
        val result =
            listSnippets(
                page = page,
                pageSize = pageSize,
                name = name,
                relation = parsedRelation,
                language = language,
                compliance = parsedCompliance,
                sortBy = parsedSortBy,
                sortDir = parsedSortDir,
            )
        log.info(
            "Listed snippets page={} pageSize={} nameFilter='{}' relation={} count={} total={}",
            page,
            pageSize,
            name ?: "",
            parsedRelation,
            result.snippets.size,
            result.total,
        )
        return ResponseEntity.ok(result.toResponse())
    }

    private inline fun <reified T : Enum<T>> parseEnumOrNull(raw: String?): T? =
        raw?.trim()?.takeIf { it.isNotEmpty() }?.let { value ->
            enumValues<T>().firstOrNull { it.name.equals(value, ignoreCase = true) }
        }

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

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Void> =
        when (deleteSnippet(SnippetId(id), UserId(jwt.subject))) {
            is DeleteSnippetResult.Deleted -> {
                log.info("Deleted snippet id={} userId={}", id, jwt.subject)
                ResponseEntity.noContent().build()
            }
            is DeleteSnippetResult.NotFound -> ResponseEntity.notFound().build()
            is DeleteSnippetResult.Forbidden -> {
                log.warn("Forbidden snippet delete id={} userId={}", id, jwt.subject)
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        }
}
