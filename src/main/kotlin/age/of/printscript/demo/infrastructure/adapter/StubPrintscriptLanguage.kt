package age.of.printscript.demo.infrastructure.adapter

import age.of.printscript.demo.domain.Language
import age.of.printscript.demo.domain.ValidationResult
import org.springframework.stereotype.Component

@Component
class StubPrintscriptLanguage : Language {
    override val name: String = "printscript"

    override fun validate(
        url: String,
        version: String,
    ): ValidationResult = ValidationResult.Success
}
