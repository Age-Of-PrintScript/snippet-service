package age.of.printscript.demo.infrastructure.controller

import age.of.printscript.demo.domain.Snippet
import age.of.printscript.demo.infrastructure.controller.dto.CreateSnippetRequest
import age.of.printscript.demo.infrastructure.controller.dto.CreateSnippetResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SnippetController {
    @PostMapping("/snippet")
    fun create(@Valid @RequestBody request: CreateSnippetRequest): CreateSnippetResponse {
        return CreateSnippetResponse("hola");
    }
}
