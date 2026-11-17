package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.domain.Language
import age.of.printscript.demo.snippet.domain.LanguageDescriptor

class GetLanguages(
    private val languages: List<Language>,
) : UseCase {
    operator fun invoke(): List<LanguageDescriptor> =
        languages.map { language ->
            LanguageDescriptor(
                name = language.name,
                fileTypes = language.fileTypes,
                versions = language.versions,
            )
        }
}
