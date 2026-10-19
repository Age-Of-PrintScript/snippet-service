package age.of.printscript.demo.infrastructure.adapter

import age.of.printscript.demo.domain.Language
import age.of.printscript.demo.domain.ValidationError
import age.of.printscript.demo.domain.ValidationResult
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class PrintscriptLanguage(
    private val printscriptRestClient: RestClient,
) : Language {
    override val name: String = "printscript"

    override fun validate(
        code: String,
        version: String,
    ): ValidationResult =
        printscriptRestClient
            .post()
            .uri("/validate/new")
            .contentType(MediaType.APPLICATION_JSON)
            .body(ValidateNewRequest(code, version))
            .exchange { _, response ->
                when (response.statusCode) {
                    HttpStatus.OK -> {
                        val body =
                            response
                                .bodyTo(ValidationErrorResponse::class.java)
                        ValidationResult.Failure(body?.errors.orEmpty())
                    }
                    HttpStatus.NO_CONTENT -> ValidationResult.Success
                    HttpStatus.BAD_REQUEST -> ValidationResult.UnsupportedVersion
                    else -> error("Error inesperado en validación: ${response.statusCode}")
                }
            } ?: error("Failed to execute validation request")

    private data class ValidateNewRequest(
        val code: String,
        val version: String,
    )

    private data class ValidationErrorResponse(
        val errors: List<ValidationError>,
    )
}
