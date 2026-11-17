package age.of.printscript.demo.testing.application

import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestContent
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DeleteSnippetTestUseCaseTest {
    private val owner = UserId("alice")
    private val snippet = createDummySnippet(ownerId = owner)
    private val snippetRepo = InMemorySnippetRepository(listOf(snippet))
    private val testRepo = InMemorySnippetTestRepository()
    private val storage = FakeSnippetTestStorage()

    private val deleteSnippetTest = DeleteSnippetTest(snippetRepo, testRepo, storage)

    private fun addTest(
        snippetId: SnippetId = snippet.id,
        status: SnippetTestStatus = SnippetTestStatus.PASSED,
    ): SnippetTest {
        val id = SnippetTestId("test-${System.nanoTime()}")
        val contentUrl = storage.save(id, SnippetTestContent(listOf("1"), listOf("1")))
        val test =
            SnippetTest(
                id = id,
                snippetId = snippetId,
                ownerId = owner,
                contentUrl = contentUrl,
                status = status,
            )
        testRepo.save(test)
        return test
    }

    @Test
    fun `deletes test from storage and repository when user is snippet owner`() {
        val test = addTest()

        val result =
            deleteSnippetTest(
                DeleteSnippetTestDto(
                    testId = test.id,
                    requesterId = owner,
                ),
            )

        assertEquals(DeleteSnippetTestResult.Success, result)
        assertNull(testRepo.findById(test.id))
        assertTrue(storage.deletedUrls.contains(test.contentUrl))
    }

    @Test
    fun `returns NotFound when test does not exist`() {
        val result =
            deleteSnippetTest(
                DeleteSnippetTestDto(
                    testId = SnippetTestId("missing"),
                    requesterId = owner,
                ),
            )

        assertEquals(DeleteSnippetTestResult.NotFound, result)
    }

    @Test
    fun `returns NotFound when snippet does not exist`() {
        val test = addTest(snippetId = SnippetId("non-existent-snippet"))

        val result =
            deleteSnippetTest(
                DeleteSnippetTestDto(
                    testId = test.id,
                    requesterId = owner,
                ),
            )

        assertEquals(DeleteSnippetTestResult.NotFound, result)
        assertEquals(test, testRepo.findById(test.id))
    }

    @Test
    fun `returns Forbidden when requester is not snippet owner`() {
        val test = addTest()

        val result =
            deleteSnippetTest(
                DeleteSnippetTestDto(
                    testId = test.id,
                    requesterId = UserId("bob"),
                ),
            )

        assertEquals(DeleteSnippetTestResult.Forbidden, result)
        assertEquals(test, testRepo.findById(test.id))
        assertTrue(storage.deletedUrls.isEmpty())
    }
}
