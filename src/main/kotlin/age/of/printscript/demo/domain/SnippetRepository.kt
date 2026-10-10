package age.of.printscript.demo.domain

interface SnippetRepository {
    fun findLatest(id: SnippetId): Snippet?
    fun findByVersion(id: SnippetId, version: Int): Snippet?
    fun findAllByOwner(ownerId: UserId): List<Snippet>
    fun save(snippet: Snippet): Snippet
}
