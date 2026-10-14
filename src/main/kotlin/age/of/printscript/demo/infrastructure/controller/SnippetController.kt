package age.of.printscript.demo.infrastructure.controller

import age.of.printscript.demo.application.GetSnippet
import age.of.printscript.demo.application.GetSnippetResult
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.SnippetWithContent
import age.of.printscript.demo.domain.UserId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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

@RestController
@RequestMapping("/snippet")
class SnippetController(
    private val getSnippet: GetSnippet,
) {
    @GetMapping("/{id}")
    fun get(
        @PathVariable id: String,
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) version: Int?,
    ): ResponseEntity<SnippetResponse> {
        val result = getSnippet(SnippetId(id), UserId(userId), version)

        return when (result) {
            is GetSnippetResult.Success -> ResponseEntity.ok(result.snippet.toResponse())
            is GetSnippetResult.NotFound -> ResponseEntity.notFound().build()
            is GetSnippetResult.Forbidden -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }
}
