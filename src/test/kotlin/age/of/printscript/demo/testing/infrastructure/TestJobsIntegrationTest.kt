package age.of.printscript.demo.testing.infrastructure

import age.of.printscript.demo.jobs.domain.Job
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobRepository
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.application.port.SnippetStorage
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.testing.application.CompleteTestJob
import age.of.printscript.demo.testing.application.port.SnippetTestStorage
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestContent
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestRepository
import age.of.printscript.demo.testing.domain.SnippetTestStatus
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration
import java.util.UUID

@EnabledIfEnvironmentVariable(named = "RUN_TEST_JOB_INTEGRATION", matches = "true")
@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:postgresql://localhost:15439/test_jobs",
        "spring.datasource.username=test_jobs",
        "spring.datasource.password=local-test-only",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=16389",
        "services.asset.url=http://localhost:18093",
        "services.printscript.url=http://localhost:18092",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.invalid/",
        "spring.security.oauth2.resourceserver.jwt.audiences=test",
    ],
)
@AutoConfigureMockMvc
@Import(TestJobsAuthenticationConfig::class)
class TestJobsIntegrationTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var snippets: SnippetRepository

    @Autowired
    private lateinit var snippetStorage: SnippetStorage

    @Autowired
    private lateinit var tests: SnippetTestRepository

    @Autowired
    private lateinit var testStorage: SnippetTestStorage

    @Autowired
    private lateinit var jobs: JobRepository

    @Autowired
    private lateinit var complete: CompleteTestJob

    @Test
    fun `ALL produces pass fail and worker error through real Redis and assets`() {
        val snippet = createSnippet()
        val passed = addTest(snippet, "hello")
        val failed = addTest(snippet, "different")
        val broken =
            SnippetTest(
                SnippetTestId(UUID.randomUUID().toString()),
                snippet.id,
                snippet.ownerId,
                "Broken test",
                "http://localhost:18093/v1/asset/snippet-tests/missing-${UUID.randomUUID()}",
            )
        tests.save(broken)

        start(snippet, """{"type":"ALL"}""")

        await().atMost(Duration.ofSeconds(30)).untilAsserted {
            assertEquals(SnippetTestStatus.PASSED, tests.findById(passed.id)?.status)
            assertEquals(SnippetTestStatus.FAILED, tests.findById(failed.id)?.status)
            assertEquals(SnippetTestStatus.ERROR, tests.findById(broken.id)?.status)
        }
        val request =
            get("/tests/statuses")
                .param("snippetId", snippet.id.value)
                .with(jwt().jwt { it.subject("alice") })
        mvc
            .perform(request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].inputs").doesNotExist())
    }

    @Test
    fun `SELECTED executes one test and keeps the other not run`() {
        val snippet = createSnippet()
        val selected = addTest(snippet, "hello")
        val other = addTest(snippet, "hello")

        start(snippet, """{"type":"SELECTED","testIds":["${selected.id.value}"]}""")

        await().atMost(Duration.ofSeconds(30)).untilAsserted {
            assertEquals(SnippetTestStatus.PASSED, tests.findById(selected.id)?.status)
        }
        assertEquals(SnippetTestStatus.NOT_RUN, tests.findById(other.id)?.status)
    }

    @Test
    fun `database guards reject old responses and reset results when a new version is saved`() {
        val snippet = createSnippet()
        val test = addTest(snippet, "hello")
        val oldJob = addJob(snippet)
        tests.markPending(test.id, oldJob.id)
        val current = addJob(snippet)
        tests.markPending(test.id, current.id)

        complete(oldJob.id, SnippetTestStatus.FAILED)
        assertEquals(current.id, tests.findById(test.id)?.activeJobId)
        assertEquals(SnippetTestStatus.PENDING, tests.findById(test.id)?.status)

        val url = snippetStorage.save(snippet.id, 2, "println(\"changed\");")
        snippets.save(snippet.copy(version = 2, contentUrl = url))
        complete(current.id, SnippetTestStatus.PASSED)

        assertEquals(2, snippets.findLatest(snippet.id)?.version)
        assertEquals(SnippetTestStatus.NOT_RUN, tests.findById(test.id)?.status)
        assertNull(tests.findById(test.id)?.activeJobId)
        assertEquals(JobStatus.DONE, jobs.findById(current.id)?.status)
    }

    private fun start(
        snippet: Snippet,
        selection: String,
    ) {
        mvc
            .perform(
                post("/tests/run")
                    .with(jwt().jwt { it.subject(snippet.ownerId.value) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"snippetId":"${snippet.id.value}","selection":$selection}"""),
            ).andExpect(status().isAccepted)
            .andExpect(jsonPath("$.testIds").isArray)
            .andExpect(jsonPath("$.runId").doesNotExist())
    }

    private fun createSnippet(): Snippet {
        val id = SnippetId(UUID.randomUUID().toString())
        val url = snippetStorage.save(id, 1, "println(\"hello\");")
        return snippets.save(createDummySnippet(id = id, contentUrl = url))
    }

    private fun addTest(
        snippet: Snippet,
        expected: String,
    ): SnippetTest {
        val id = SnippetTestId(UUID.randomUUID().toString())
        val url = testStorage.save(id, SnippetTestContent(emptyList(), listOf(expected)))
        val test = SnippetTest(id, snippet.id, snippet.ownerId, "Expected $expected", url)
        tests.save(test)
        return test
    }

    private fun addJob(snippet: Snippet): Job =
        jobs.save(
            Job(
                JobId(UUID.randomUUID().toString()),
                snippet.id,
                snippet.version,
                snippet.ownerId,
                JobType("TEST"),
                JobStatus.PENDING,
            ),
        )
}

@TestConfiguration
class TestJobsAuthenticationConfig {
    @Bean
    fun jwtDecoder(): JwtDecoder = JwtDecoder { throw BadJwtException("Use the test JWT request processor") }
}
