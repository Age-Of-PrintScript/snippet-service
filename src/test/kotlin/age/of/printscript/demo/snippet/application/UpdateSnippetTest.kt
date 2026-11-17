package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.jobs.application.FakeJobPublisher
import age.of.printscript.demo.jobs.application.InMemoryJobRepository
import age.of.printscript.demo.lint.application.InMemoryLintRuleRepository
import age.of.printscript.demo.lint.application.InMemorySnippetLintStatusRepository
import age.of.printscript.demo.lint.application.LintSnippet
import age.of.printscript.demo.lint.domain.LintStatus
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetPage
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.snippet.domain.ValidationError
import age.of.printscript.demo.snippet.domain.ValidationResult
import age.of.printscript.demo.testing.application.CompleteTestJob
import age.of.printscript.demo.testing.application.FakeTestJobPublisher
import age.of.printscript.demo.testing.application.InMemorySnippetTestRepository
import age.of.printscript.demo.testing.application.RunSnippetTests
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UpdateSnippetTest {
    private val initialCode = "let a: number = 5;"
    private val updatedCode = "let a: number = 10;\nprintln(a);"

    @Test
    fun `returns Updated and persists new version as latest while marking previous version as not latest`() {
        val existing = createDummySnippet(ownerId = UserId("alice"), version = 1)
        val repo = InMemorySnippetRepository(listOf(existing))
        val storage = FakeSnippetStorage().apply { set(existing.contentUrl, initialCode) }
        val language = FakeLanguage(name = "printscript", result = ValidationResult.Success)
        val updateSnippet = UpdateSnippet(repo, storage, listOf(language))

        val result = updateSnippet(existing.id, UserId("alice"), updatedCode)

        assertTrue(result is UpdateSnippetResult.Updated)
        val updated = (result as UpdateSnippetResult.Updated).snippet
        assertEquals(existing.id, updated.id)
        assertEquals(2, updated.version)
        assertEquals(existing.title, updated.title)
        assertEquals(existing.description, updated.description)
        assertEquals(UserId("alice"), updated.ownerId)
        assertEquals(existing.language, updated.language)
        assertEquals(existing.languageVersion, updated.languageVersion)
        assertEquals(updatedCode, updated.content)
        assertTrue(updated.isLatest)

        val latestPersisted = requireNotNull(repo.findLatest(existing.id))
        assertEquals(2, latestPersisted.version)
        assertTrue(latestPersisted.isLatest)
        assertEquals(updatedCode, storage.read(latestPersisted.contentUrl))

        val previousVersion = requireNotNull(repo.findByVersion(existing.id, 1))
        assertFalse(previousVersion.isLatest)
        assertEquals(initialCode, storage.read(previousVersion.contentUrl))
    }

    @Test
    fun `returns NotFound when snippet does not exist`() {
        val repo = InMemorySnippetRepository()
        val storage = FakeSnippetStorage()
        val language = FakeLanguage(name = "printscript")
        val updateSnippet = UpdateSnippet(repo, storage, listOf(language))

        val result = updateSnippet(SnippetId("non-existent"), UserId("alice"), updatedCode)

        assertEquals(UpdateSnippetResult.NotFound, result)
        assertTrue(storage.isEmpty())
    }

    @Test
    fun `returns Forbidden when user is not the owner`() {
        val existing = createDummySnippet(ownerId = UserId("alice"), version = 1)
        val repo = InMemorySnippetRepository(listOf(existing))
        val storage = FakeSnippetStorage()
        val language = FakeLanguage(name = "printscript")
        val updateSnippet = UpdateSnippet(repo, storage, listOf(language))

        val result = updateSnippet(existing.id, UserId("bob"), updatedCode)

        assertEquals(UpdateSnippetResult.Forbidden, result)
        assertTrue(storage.isEmpty())
        assertEquals(1, repo.findLatest(existing.id)?.version)
    }

    @Test
    fun `returns InvalidSnippet without saving to storage when parser rejects code`() {
        val existing = createDummySnippet(ownerId = UserId("alice"), version = 1)
        val repo = InMemorySnippetRepository(listOf(existing))
        val storage = FakeSnippetStorage()
        val errors = listOf(ValidationError("Missing semicolon"))
        val language = FakeLanguage(name = "printscript", result = ValidationResult.Failure(errors))
        val updateSnippet = UpdateSnippet(repo, storage, listOf(language))

        val result = updateSnippet(existing.id, UserId("alice"), "let a: number = 5")

        assertEquals(UpdateSnippetResult.InvalidSnippet(errors), result)
        assertTrue(storage.isEmpty())
        assertEquals(1, repo.findLatest(existing.id)?.version)
    }

    @Test
    fun `returns UnsupportedLanguage without saving when snippet language is not registered`() {
        val existing = createDummySnippet(ownerId = UserId("alice"), version = 1).copy(language = "python")
        val repo = InMemorySnippetRepository(listOf(existing))
        val storage = FakeSnippetStorage()
        val language = FakeLanguage(name = "printscript")
        val updateSnippet = UpdateSnippet(repo, storage, listOf(language))

        val result = updateSnippet(existing.id, UserId("alice"), updatedCode)

        assertEquals(UpdateSnippetResult.UnsupportedLanguage("python"), result)
        assertTrue(storage.isEmpty())
        assertEquals(1, repo.findLatest(existing.id)?.version)
    }

    @Test
    fun `returns UnsupportedVersion without saving when language version is not supported`() {
        val existing = createDummySnippet(ownerId = UserId("alice"), version = 1).copy(languageVersion = "9.9")
        val repo = InMemorySnippetRepository(listOf(existing))
        val storage = FakeSnippetStorage()
        val language = FakeLanguage(name = "printscript", result = ValidationResult.UnsupportedVersion)
        val updateSnippet = UpdateSnippet(repo, storage, listOf(language))

        val result = updateSnippet(existing.id, UserId("alice"), updatedCode)

        assertEquals(UpdateSnippetResult.UnsupportedVersion("9.9"), result)
        assertTrue(storage.isEmpty())
        assertEquals(1, repo.findLatest(existing.id)?.version)
    }

    @Test
    fun `deletes stored content and rethrows when repository throws unexpected exception`() {
        val existing = createDummySnippet(ownerId = UserId("alice"), version = 1)
        val failingRepo =
            object : SnippetRepository {
                override fun findLatest(id: SnippetId): Snippet? = existing

                override fun findByVersion(
                    id: SnippetId,
                    version: Int,
                ): Snippet? = null

                override fun findAllByOwner(ownerId: UserId): List<Snippet> = emptyList()

                override fun findLatestByIds(
                    ids: List<SnippetId>,
                    name: String?,
                    page: Int,
                    pageSize: Int,
                ): SnippetPage = SnippetPage(emptyList(), 0)

                override fun findAllVersions(id: SnippetId): List<Snippet> = emptyList()

                override fun deleteById(id: SnippetId) = Unit

                override fun save(snippet: Snippet): Snippet = error("Database unavailable")
            }
        val storage = FakeSnippetStorage()
        val language = FakeLanguage(name = "printscript", result = ValidationResult.Success)
        val updateSnippet = UpdateSnippet(failingRepo, storage, listOf(language))

        assertThrows(IllegalStateException::class.java) {
            updateSnippet(existing.id, UserId("alice"), updatedCode)
        }

        assertTrue(storage.isEmpty())
    }

    @Test
    fun `enqueues lint job for new version after updating snippet`() {
        val existing = createDummySnippet(ownerId = UserId("alice"), version = 1)
        val repo = InMemorySnippetRepository(listOf(existing))
        val storage = FakeSnippetStorage()
        val publisher = FakeJobPublisher()
        val lintStatusRepo = InMemorySnippetLintStatusRepository()
        val lintSnippet =
            LintSnippet(
                jobPublisher = publisher,
                snippetRepository = repo,
                lintRuleRepository = InMemoryLintRuleRepository(),
                snippetLintStatusRepository = lintStatusRepo,
                jobRepository = InMemoryJobRepository(),
            )
        val language = FakeLanguage(name = "printscript", result = ValidationResult.Success)
        val updateSnippet = UpdateSnippet(repo, storage, listOf(language), lintSnippet)

        val result = updateSnippet(existing.id, UserId("alice"), updatedCode)

        val updated = (result as UpdateSnippetResult.Updated).snippet
        assertEquals(2, updated.version)
        assertEquals(1, publisher.publishedLintJobs.size)
        val job = publisher.publishedLintJobs.first()
        assertEquals(2, job.snippet.version)
        assertEquals(LintStatus.PENDING, lintStatusRepo.findByVersion(existing.id, 2)?.status)
    }

    @Test
    fun `enqueues all tests for new version after updating snippet`() {
        val existing = createDummySnippet(ownerId = UserId("alice"), version = 1)
        val repo = InMemorySnippetRepository(listOf(existing))
        val storage = FakeSnippetStorage()
        val testsRepo = InMemorySnippetTestRepository()
        val publisher = FakeTestJobPublisher()
        val jobsRepo = InMemoryJobRepository()
        val runSnippetTests =
            RunSnippetTests(
                snippets = repo,
                tests = testsRepo,
                permissions = FakePermissionGateway(),
                jobs = jobsRepo,
                publisher = publisher,
                completeTestJob = CompleteTestJob(jobsRepo, testsRepo),
            )
        val test1 =
            SnippetTest(
                id = SnippetTestId("test-1"),
                snippetId = existing.id,
                ownerId = UserId("alice"),
                contentUrl = "url-test-1",
            )
        val test2 =
            SnippetTest(
                id = SnippetTestId("test-2"),
                snippetId = existing.id,
                ownerId = UserId("alice"),
                contentUrl = "url-test-2",
            )
        testsRepo.save(test1)
        testsRepo.save(test2)

        val language = FakeLanguage(name = "printscript", result = ValidationResult.Success)
        val updateSnippet =
            UpdateSnippet(
                snippetRepository = repo,
                snippetStorage = storage,
                languages = listOf(language),
                runSnippetTests = runSnippetTests,
            )

        val result = updateSnippet(existing.id, UserId("alice"), updatedCode)

        val updated = (result as UpdateSnippetResult.Updated).snippet
        assertEquals(2, updated.version)
        assertEquals(2, publisher.published.size)
        assertEquals(listOf("url-test-1", "url-test-2"), publisher.published.map { it.testUrl })
        publisher.published.forEach {
            assertEquals(2, it.snippet.version)
        }
    }

    @Test
    fun `continues successfully when test publisher throws exception`() {
        val existing = createDummySnippet(ownerId = UserId("alice"), version = 1)
        val repo = InMemorySnippetRepository(listOf(existing))
        val storage = FakeSnippetStorage()
        val testsRepo = InMemorySnippetTestRepository()
        val publisher = FakeTestJobPublisher().apply { failingUrls += "url-test-fail" }
        val jobsRepo = InMemoryJobRepository()
        val runSnippetTests =
            RunSnippetTests(
                snippets = repo,
                tests = testsRepo,
                permissions = FakePermissionGateway(),
                jobs = jobsRepo,
                publisher = publisher,
                completeTestJob = CompleteTestJob(jobsRepo, testsRepo),
            )
        val test =
            SnippetTest(
                id = SnippetTestId("test-fail"),
                snippetId = existing.id,
                ownerId = UserId("alice"),
                contentUrl = "url-test-fail",
            )
        testsRepo.save(test)

        val language = FakeLanguage(name = "printscript", result = ValidationResult.Success)
        val updateSnippet =
            UpdateSnippet(
                snippetRepository = repo,
                snippetStorage = storage,
                languages = listOf(language),
                runSnippetTests = runSnippetTests,
            )

        val result = updateSnippet(existing.id, UserId("alice"), updatedCode)

        assertTrue(result is UpdateSnippetResult.Updated)
        val updated = (result as UpdateSnippetResult.Updated).snippet
        assertEquals(2, updated.version)
        assertEquals(2, repo.findLatest(existing.id)?.version)
    }
}
