package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CreateSnippetTestUseCaseTest {
    private fun request(
        snippetId: SnippetId = SnippetId("snippet-1"),
        ownerId: UserId = UserId("alice"),
        inputs: List<String> = listOf("2", "3"),
        outputs: List<String> = listOf("5"),
    ) = CreateSnippetTestDto(
        snippetId = snippetId,
        ownerId = ownerId,
        inputs = inputs,
        outputs = outputs,
    )

    @Test
    fun `creates test with ordered inputs and outputs for its owner`() {
        val snippet = createDummySnippet()
        val testRepository = InMemorySnippetTestRepository()
        val storage = FakeSnippetTestStorage()
        val createTest = CreateSnippetTest(InMemorySnippetRepository(listOf(snippet)), testRepository, storage)

        val result = createTest(request())

        assertTrue(result is CreateSnippetTestResult.Created)
        val persistedTest = testRepository.savedTests.single().first
        assertEquals(listOf("2", "3"), persistedTest.inputs)
        assertEquals(listOf("5"), persistedTest.outputs)
        assertEquals(snippet.id, persistedTest.snippetId)
        assertEquals(UserId("alice"), persistedTest.ownerId)
    }

    @Test
    fun `creates test when expected outputs do not match snippet behavior`() {
        val snippet = createDummySnippet()
        val testRepository = InMemorySnippetTestRepository()
        val storage = FakeSnippetTestStorage()
        val createTest = CreateSnippetTest(InMemorySnippetRepository(listOf(snippet)), testRepository, storage)

        val result = createTest(request(inputs = listOf("2"), outputs = listOf("999")))

        assertTrue(result is CreateSnippetTestResult.Created)
        assertEquals(
            listOf("999"),
            testRepository.savedTests
                .single()
                .first.outputs,
        )
    }

    @Test
    fun `returns NotFound without storing test when snippet does not exist`() {
        val testRepository = InMemorySnippetTestRepository()
        val storage = FakeSnippetTestStorage()
        val createTest = CreateSnippetTest(InMemorySnippetRepository(), testRepository, storage)

        val result = createTest(request())

        assertEquals(CreateSnippetTestResult.NotFound, result)
        assertTrue(testRepository.savedTests.isEmpty())
        assertTrue(storage.isEmpty())
    }

    @Test
    fun `returns Forbidden without storing test when user is not snippet owner`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val testRepository = InMemorySnippetTestRepository()
        val storage = FakeSnippetTestStorage()
        val createTest = CreateSnippetTest(InMemorySnippetRepository(listOf(snippet)), testRepository, storage)

        val result = createTest(request(ownerId = UserId("bob")))

        assertEquals(CreateSnippetTestResult.Forbidden, result)
        assertTrue(testRepository.savedTests.isEmpty())
        assertTrue(storage.isEmpty())
    }

    @Test
    fun `deletes stored test and rethrows when persistence fails`() {
        val snippet = createDummySnippet()
        val testRepository =
            InMemorySnippetTestRepository(
                SnippetTestPersistenceException(IllegalStateException("Database unavailable")),
            )
        val storage = FakeSnippetTestStorage()
        val createTest = CreateSnippetTest(InMemorySnippetRepository(listOf(snippet)), testRepository, storage)

        assertThrows(SnippetTestPersistenceException::class.java) {
            createTest(request())
        }

        assertTrue(storage.isEmpty())
        assertEquals(1, storage.deletedUrls.size)
    }
}
