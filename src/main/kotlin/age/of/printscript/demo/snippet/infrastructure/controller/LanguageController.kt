package age.of.printscript.demo.snippet.infrastructure.controller

import age.of.printscript.demo.snippet.application.GetLanguages
import age.of.printscript.demo.snippet.domain.LanguageDescriptor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class LanguageResponse(
    val language: String,
    val fileTypes: List<String>,
    val versions: List<String>,
)

private fun LanguageDescriptor.toResponse() =
    LanguageResponse(
        language = name,
        fileTypes = fileTypes,
        versions = versions,
    )

@RestController
@RequestMapping("/languages")
class LanguageController(
    private val getLanguages: GetLanguages,
) {
    @GetMapping
    fun list(): ResponseEntity<List<LanguageResponse>> = ResponseEntity.ok(getLanguages().map { it.toResponse() })
}
