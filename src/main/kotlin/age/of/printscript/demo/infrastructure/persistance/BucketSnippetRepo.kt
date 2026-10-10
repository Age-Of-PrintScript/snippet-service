package age.of.printscript.demo.infrastructure.persistance

import age.of.printscript.demo.domain.Snippet
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.SnippetRepository
import age.of.printscript.demo.domain.UserId

class BucketSnippetRepo : SnippetRepository {
    override fun findLatest(id: SnippetId): Snippet? {
        TODO("Not yet implemented")
    }

    override fun findByVersion(
        id: SnippetId,
        version: Int,
    ): Snippet? {
        TODO("Not yet implemented")
    }

    override fun findAllByOwner(ownerId: UserId): List<Snippet> {
        TODO("Not yet implemented")
    }

    override fun save(snippet: Snippet): Snippet {
        TODO("Not yet implemented")
    }
}
