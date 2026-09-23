package age.of.printscript.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SnippetController {
    @GetMapping("/snippet")
    fun create(
        @RequestParam snippet: Snippet,
    ): Snippet {
        println(snippet)
        TODO("not implemented")
    }
}
