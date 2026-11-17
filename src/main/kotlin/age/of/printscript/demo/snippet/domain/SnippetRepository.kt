package age.of.printscript.demo.snippet.domain

data class SnippetPage(
    val items: List<Snippet>,
    val total: Long,
)

interface SnippetRepository {
    fun findLatest(id: SnippetId): Snippet?

    fun findByVersion(
        id: SnippetId,
        version: Int,
    ): Snippet?

    fun findAllByOwner(ownerId: UserId): List<Snippet>

    fun findLatestByIds(
        ids: List<SnippetId>,
        name: String?,
        page: Int,
        pageSize: Int,
    ): SnippetPage

    fun save(snippet: Snippet): Snippet
}
