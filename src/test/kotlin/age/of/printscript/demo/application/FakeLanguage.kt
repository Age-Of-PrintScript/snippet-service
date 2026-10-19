package age.of.printscript.demo.application

import age.of.printscript.demo.domain.Language
import age.of.printscript.demo.domain.ValidationResult

class FakeLanguage(
    override val name: String = "printscript",
    private val result: ValidationResult = ValidationResult.Success,
) : Language {
    override fun validate(
        code: String,
        version: String,
    ): ValidationResult = result
}
