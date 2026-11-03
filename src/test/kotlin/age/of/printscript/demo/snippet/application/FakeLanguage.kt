package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.snippet.domain.Language
import age.of.printscript.demo.snippet.domain.ValidationResult

class FakeLanguage(
    override val name: String = "printscript",
    private val result: ValidationResult = ValidationResult.Success,
) : Language {
    override fun validate(
        code: String,
        version: String,
    ): ValidationResult = result
}
