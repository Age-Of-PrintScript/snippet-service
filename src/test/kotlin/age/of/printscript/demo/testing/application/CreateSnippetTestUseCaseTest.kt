package age.of.printscript.demo.testing.application

import age.of.printscript.demo.jobs.domain.JobId
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
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CreateSnippetTestUseCaseTest {
    private fun request(
        snippetId: SnippetId = SnippetId("snippet-1"),
        ownerId: UserId = UserId("alice"),
        name: String = "Test",
        inputs: List<String> = listOf("2", "3"),
        outputs: List<String> = listOf("5"),
    ) = CreateSnippetTestDto(
        snippetId = snippetId,
        ownerId = ownerId,
        name = name,
        inputs = inputs,
        outputs = outputs,
    )

    @Test
    fun `creates test with ordered inputs and outputs for its owner`() {
        val snippet = createDummySnippet()
        val testRepository = InMemorySnippetTestRepository()
        val storage = FakeSnippetTestStorage()
        val createTest = CreateSnippetTest(InMemorySnippetRepository(listOf(snippet)), testRepository, storage)

        val result = createTest(request(name = "Sum test"))

        assertTrue(result is CreateSnippetTestResult.Created)
        val createdSummary = (result as CreateSnippetTestResult.Created).test
        assertEquals("Sum test", createdSummary.name)
        val persistedTest = testRepository.savedTests.single().first
        assertEquals(snippet.id, persistedTest.snippetId)
        assertEquals(UserId("alice"), persistedTest.ownerId)
        assertEquals("Sum test", persistedTest.name)
        assertEquals(
            SnippetTestContent(listOf("2", "3"), listOf("5")),
            storage.read(persistedTest.contentUrl),
        )
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
            storage
                .read(
                    testRepository.savedTests
                        .single()
                        .first.contentUrl,
                ).outputs,
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

    @Test
    fun `updates existing test, overwrites content and sets status to NOT_RUN when id is provided`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val testRepository = InMemorySnippetTestRepository()
        val storage = FakeSnippetTestStorage()
        val testId = SnippetTestId("test-1")
        val oldUrl = storage.save(testId, SnippetTestContent(listOf("1"), listOf("1")))
        testRepository.save(
            SnippetTest(
                id = testId,
                snippetId = snippet.id,
                ownerId = snippet.ownerId,
                name = "Old test",
                contentUrl = oldUrl,
                status = SnippetTestStatus.PASSED,
                activeJobId = JobId("old-job"),
            ),
        )
        val createTest = CreateSnippetTest(InMemorySnippetRepository(listOf(snippet)), testRepository, storage)

        val result =
            createTest(
                request(
                    name = "Updated sum test",
                    inputs = listOf("10", "20"),
                    outputs = listOf("30"),
                ).copy(id = testId),
            )

        assertTrue(result is CreateSnippetTestResult.Created)
        val updatedSummary = (result as CreateSnippetTestResult.Created).test
        assertEquals(testId, updatedSummary.id)
        assertEquals("Updated sum test", updatedSummary.name)
        assertEquals(SnippetTestStatus.NOT_RUN, updatedSummary.status)

        val persisted = testRepository.findById(testId)
        assertEquals("Updated sum test", persisted?.name)
        assertEquals(SnippetTestStatus.NOT_RUN, persisted?.status)
        assertNull(persisted?.activeJobId)
        assertEquals(
            SnippetTestContent(listOf("10", "20"), listOf("30")),
            storage.read(requireNotNull(persisted?.contentUrl)),
        )
    }

    @Test
    fun `returns NotFound when updating a test id that does not exist`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val testRepository = InMemorySnippetTestRepository()
        val storage = FakeSnippetTestStorage()
        val createTest = CreateSnippetTest(InMemorySnippetRepository(listOf(snippet)), testRepository, storage)

        val result = createTest(request().copy(id = SnippetTestId("missing")))

        assertEquals(CreateSnippetTestResult.NotFound, result)
    }

    @Test
    fun `returns NotFound when test id belongs to a different snippet`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"), id = SnippetId("snip-1"))
        val otherSnippet = createDummySnippet(ownerId = UserId("alice"), id = SnippetId("snip-2"))
        val testRepository = InMemorySnippetTestRepository()
        val storage = FakeSnippetTestStorage()
        val testId = SnippetTestId("test-diff-snip")
        testRepository.save(
            SnippetTest(
                id = testId,
                snippetId = otherSnippet.id,
                ownerId = snippet.ownerId,
                name = "Other test",
                contentUrl = "url",
            ),
        )
        val createTest =
            CreateSnippetTest(
                InMemorySnippetRepository(listOf(snippet, otherSnippet)),
                testRepository,
                storage,
            )

        val result = createTest(request(snippetId = snippet.id).copy(id = testId))

        assertEquals(CreateSnippetTestResult.NotFound, result)
    }

    @Test
    fun `returns Forbidden when updating existing test if requester is not snippet owner`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val testRepository = InMemorySnippetTestRepository()
        val storage = FakeSnippetTestStorage()
        val testId = SnippetTestId("test-1")
        testRepository.save(
            SnippetTest(
                id = testId,
                snippetId = snippet.id,
                ownerId = snippet.ownerId,
                name = "Alice test",
                contentUrl = "url",
            ),
        )
        val createTest = CreateSnippetTest(InMemorySnippetRepository(listOf(snippet)), testRepository, storage)

        val result = createTest(request(ownerId = UserId("bob")).copy(id = testId))

        assertEquals(CreateSnippetTestResult.Forbidden, result)
    }
}
