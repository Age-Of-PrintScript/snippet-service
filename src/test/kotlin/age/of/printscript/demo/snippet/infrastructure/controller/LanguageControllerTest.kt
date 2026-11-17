package age.of.printscript.demo.snippet.infrastructure.controller

import age.of.printscript.demo.snippet.application.FakeLanguage
import age.of.printscript.demo.snippet.application.GetLanguages
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class LanguageControllerTest {
    @Test
    fun `list returns 200 OK with registered languages, fileTypes and versions`() {
        val languages =
            listOf(
                FakeLanguage(
                    name = "printscript",
                    fileTypes = listOf("prs", "ps"),
                    versions = listOf("1.0", "1.1"),
                ),
                FakeLanguage(
                    name = "python",
                    fileTypes = listOf("py"),
                    versions = listOf("3.12"),
                ),
            )
        val controller = LanguageController(GetLanguages(languages))

        val response = controller.list()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(
            listOf(
                LanguageResponse("printscript", listOf("prs", "ps"), listOf("1.0", "1.1")),
                LanguageResponse("python", listOf("py"), listOf("3.12")),
            ),
            response.body,
        )
    }
}
