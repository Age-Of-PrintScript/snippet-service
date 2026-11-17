package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.lint.application.LintSnippet
import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.application.port.PermissionGateway
import age.of.printscript.demo.snippet.application.port.SnippetStorage
import age.of.printscript.demo.snippet.domain.Language
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.SnippetWithContent
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.snippet.domain.ValidationError
import age.of.printscript.demo.snippet.domain.ValidationResult
import age.of.printscript.demo.snippet.domain.withContent
import org.slf4j.LoggerFactory
import java.util.UUID

class CreateSnippet(
    private val snippetRepository: SnippetRepository,
    private val snippetStorage: SnippetStorage,
    private val languages: List<Language>,
    private val permissionGateway: PermissionGateway,
    private val lintSnippet: LintSnippet? = null,
) : UseCase {
    private val logger = LoggerFactory.getLogger(javaClass)

    operator fun invoke(
        createSnippet: CreateSnippetDto,
        content: String,
    ): CreateSnippetResult {
        val language =
            languages.find { it.name.equals(createSnippet.language, ignoreCase = true) }
                ?: return CreateSnippetResult.UnsupportedLanguage(createSnippet.language)
        val languageVersion = createSnippet.languageVersion
        val id = SnippetId(UUID.randomUUID().toString())
        val version = 1
        val contentUrl = snippetStorage.save(id, version, content)

        return runCatching {
            when (val validation = language.validate(content, languageVersion)) {
                is ValidationResult.Failure -> {
                    snippetStorage.delete(contentUrl)
                    CreateSnippetResult.InvalidSnippet(validation.errors)
                }
                is ValidationResult.UnsupportedVersion -> {
                    snippetStorage.delete(contentUrl)
                    CreateSnippetResult.UnsupportedVersion(languageVersion)
                }
                is ValidationResult.Success -> {
                    // Si falla, runCatching borra el contenido y no se guarda el snippet.
                    permissionGateway.registerOwner(id)
                    val snippet = snippetFromDto(createSnippet, id, contentUrl, version)
                    val saved = snippetRepository.save(snippet)
                    runCatching { lintSnippet?.invoke(saved.id, saved.ownerId) }
                        .onFailure { logger.warn("Failed to enqueue lint job for snippet {}", saved.id.value, it) }
                    CreateSnippetResult.Created(saved.withContent(content))
                }
            }
        }.onFailure {
            snippetStorage.delete(contentUrl)
        }.getOrThrow()
    }

    private fun snippetFromDto(
        createSnippet: CreateSnippetDto,
        id: SnippetId,
        contentUrl: String,
        version: Int,
    ): Snippet =
        Snippet(
            id = id,
            version = version,
            title = createSnippet.title,
            description = createSnippet.description,
            ownerId = createSnippet.ownerId,
            language = createSnippet.language.lowercase(),
            languageVersion = createSnippet.languageVersion,
            contentUrl = contentUrl,
        )
}

data class CreateSnippetDto(
    val title: String,
    val description: String?,
    val ownerId: UserId,
    val language: String,
    val languageVersion: String,
)

sealed interface CreateSnippetResult {
    data class Created(
        val snippet: SnippetWithContent,
    ) : CreateSnippetResult

    data class InvalidSnippet(
        val errors: List<ValidationError>,
    ) : CreateSnippetResult

    data class UnsupportedLanguage(
        val language: String,
    ) : CreateSnippetResult

    data class UnsupportedVersion(
        val version: String,
    ) : CreateSnippetResult
}
