package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.shared.application.UseCase
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

class UpdateSnippet(
    private val snippetRepository: SnippetRepository,
    private val snippetStorage: SnippetStorage,
    private val languages: List<Language>,
) : UseCase {
    operator fun invoke(
        id: SnippetId,
        userId: UserId,
        content: String,
    ): UpdateSnippetResult {
        val current = snippetRepository.findLatest(id) ?: return UpdateSnippetResult.NotFound
        return if (current.ownerId != userId) {
            UpdateSnippetResult.Forbidden
        } else {
            validateAndSave(current, content)
        }
    }

    private fun validateAndSave(
        current: Snippet,
        content: String,
    ): UpdateSnippetResult {
        val language =
            languages.find { it.name.equals(current.language, ignoreCase = true) }
                ?: return UpdateSnippetResult.UnsupportedLanguage(current.language)

        return when (val validation = language.validate(content, current.languageVersion)) {
            is ValidationResult.Failure -> UpdateSnippetResult.InvalidSnippet(validation.errors)
            is ValidationResult.UnsupportedVersion ->
                UpdateSnippetResult.UnsupportedVersion(current.languageVersion)
            is ValidationResult.Success -> saveNewVersion(current, content)
        }
    }

    private fun saveNewVersion(
        current: Snippet,
        content: String,
    ): UpdateSnippetResult {
        val newVersion = current.version + 1
        val contentUrl = snippetStorage.save(current.id, newVersion, content)
        return runCatching {
            val updated =
                current.copy(
                    version = newVersion,
                    contentUrl = contentUrl,
                    isLatest = true,
                )
            val saved = snippetRepository.save(updated)
            UpdateSnippetResult.Updated(saved.withContent(content))
        }.onFailure {
            snippetStorage.delete(contentUrl)
        }.getOrThrow()
    }
}

sealed interface UpdateSnippetResult {
    data class Updated(
        val snippet: SnippetWithContent,
    ) : UpdateSnippetResult

    data class InvalidSnippet(
        val errors: List<ValidationError>,
    ) : UpdateSnippetResult

    data class UnsupportedLanguage(
        val language: String,
    ) : UpdateSnippetResult

    data class UnsupportedVersion(
        val version: String,
    ) : UpdateSnippetResult

    data object NotFound : UpdateSnippetResult

    data object Forbidden : UpdateSnippetResult
}
