package age.of.printscript.demo.application

import age.of.printscript.demo.domain.Snippet
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.SnippetRepository
import age.of.printscript.demo.domain.UserId

class InMemorySnippetRepository(
    initialSnippets: List<Snippet> = emptyList(),
) : SnippetRepository {
    private val snippets = initialSnippets.toMutableList()

    override fun findLatest(id: SnippetId): Snippet? =
        snippets
            .filter { it.id == id && it.isLatest }
            .maxByOrNull { it.version }

    override fun findByVersion(
        id: SnippetId,
        version: Int,
    ): Snippet? =
        snippets.find {
            it.id == id && it.version == version
        }

    override fun findAllByOwner(ownerId: UserId): List<Snippet> =
        snippets.filter {
            it.ownerId == ownerId && it.isLatest
        }

    override fun save(snippet: Snippet): Snippet {
        snippets.removeIf {
            it.id == snippet.id && it.version == snippet.version
        }
        snippets.add(snippet)
        return snippet
    }
}
