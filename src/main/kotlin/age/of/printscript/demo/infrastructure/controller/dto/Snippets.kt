package age.of.printscript.demo.infrastructure.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateSnippetRequest(
    @field:NotBlank
    @field:Size(min = 1, max = 100)
    val title: String,
    val description: String,
    @field:NotBlank
    val language: String,
    @field:NotBlank
    val version: String,
)

data class CreateSnippetResponse(
    val title: String,
)
