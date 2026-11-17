package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetPage
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId

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

    override fun findLatestByIds(
        ids: List<SnippetId>,
        name: String?,
        page: Int,
        pageSize: Int,
    ): SnippetPage {
        val idSet = ids.toSet()
        val filter = name?.trim()?.takeIf { it.isNotEmpty() }
        val matching =
            snippets.filter {
                it.isLatest &&
                    it.id in idSet &&
                    (filter == null || it.title.contains(filter, ignoreCase = true))
            }
        val fromIndex = (page.coerceAtLeast(0) * pageSize.coerceAtLeast(1)).coerceAtMost(matching.size)
        val toIndex = (fromIndex + pageSize.coerceAtLeast(1)).coerceAtMost(matching.size)
        return SnippetPage(matching.subList(fromIndex, toIndex), matching.size.toLong())
    }

    override fun findAllVersions(id: SnippetId): List<Snippet> =
        snippets
            .filter { it.id == id }
            .sortedByDescending { it.version }

    override fun deleteById(id: SnippetId) {
        snippets.removeIf { it.id == id }
    }

    override fun save(snippet: Snippet): Snippet {
        if (snippet.isLatest) {
            snippets.replaceAll {
                if (it.id == snippet.id && it.isLatest) it.copy(isLatest = false) else it
            }
        }
        snippets.removeIf {
            it.id == snippet.id && it.version == snippet.version
        }
        snippets.add(snippet)
        return snippet
    }
}
