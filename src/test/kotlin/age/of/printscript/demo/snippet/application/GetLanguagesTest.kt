package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.snippet.domain.LanguageDescriptor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetLanguagesTest {
    @Test
    fun `returns descriptors for all registered languages`() {
        val printscript =
            FakeLanguage(
                name = "printscript",
                fileTypes = listOf("prs", "ps"),
                versions = listOf("1.0", "1.1"),
            )
        val python =
            FakeLanguage(
                name = "python",
                fileTypes = listOf("py"),
                versions = listOf("3.11", "3.12"),
            )
        val useCase = GetLanguages(listOf(printscript, python))

        val result = useCase()

        assertEquals(
            listOf(
                LanguageDescriptor("printscript", listOf("prs", "ps"), listOf("1.0", "1.1")),
                LanguageDescriptor("python", listOf("py"), listOf("3.11", "3.12")),
            ),
            result,
        )
    }

    @Test
    fun `returns empty list when no languages are registered`() {
        val useCase = GetLanguages(emptyList())

        assertEquals(emptyList<LanguageDescriptor>(), useCase())
    }
}
