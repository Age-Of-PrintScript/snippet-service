package age.of.printscript.demo.testing.infrastructure.controller

import age.of.printscript.demo.jobs.application.InMemoryJobRepository
import age.of.printscript.demo.snippet.application.FakePermissionGateway
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.application.testJwt
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.testing.application.CompleteTestJob
import age.of.printscript.demo.testing.application.FakeSnippetTestStorage
import age.of.printscript.demo.testing.application.FakeTestJobPublisher
import age.of.printscript.demo.testing.application.InMemorySnippetTestRepository
import age.of.printscript.demo.testing.application.ListSnippetTestStatuses
import age.of.printscript.demo.testing.application.ListSnippetTests
import age.of.printscript.demo.testing.application.RunSnippetTests
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.UUID

class TestsRunControllerTest {
    private val snippet = createDummySnippet(id = SnippetId(UUID.randomUUID().toString()))
    private val snippets = InMemorySnippetRepository(listOf(snippet))
    private val tests = InMemorySnippetTestRepository()
    private val publisher = FakeTestJobPublisher()
    private val permissions = FakePermissionGateway()
    private val jobs = InMemoryJobRepository()
    private val runTests = RunSnippetTests(snippets, tests, permissions, jobs, publisher, CompleteTestJob(jobs, tests))
    private val mvc =
        MockMvcBuilders
            .standaloneSetup(
                TestsRunController(runTests),
                TestsQueryController(
                    ListSnippetTests(snippets, tests, FakeSnippetTestStorage(), permissions),
                    ListSnippetTestStatuses(snippets, tests, permissions),
                ),
            ).setCustomArgumentResolvers(AuthenticationPrincipalArgumentResolver())
            .build()

    @BeforeEach
    fun authenticate() {
        SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(testJwt(snippet.ownerId.value))
    }

    @AfterEach
    fun clearAuthentication() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `POST ALL without testIds returns 202 with ids and polling shows pending`() {
        val first = addTest()
        val second = addTest()

        mvc
            .perform(
                post("/tests/run")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"snippetId":"${snippet.id.value}","selection":{"type":"ALL"}}"""),
            ).andExpect(status().isAccepted)
            .andExpect(jsonPath("$.testIds[0]").value(first.value))
            .andExpect(jsonPath("$.testIds[1]").value(second.value))
            .andExpect(jsonPath("$.runId").doesNotExist())

        mvc
            .perform(get("/tests/statuses").param("snippetId", snippet.id.value))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(first.value))
            .andExpect(jsonPath("$[0].status").value("PENDING"))
            .andExpect(jsonPath("$[1].status").value("PENDING"))
            .andExpect(jsonPath("$[0].inputs").doesNotExist())
            .andExpect(jsonPath("$[0].outputs").doesNotExist())
        assertEquals(2, publisher.published.size)
    }

    @Test
    fun `POST SELECTED returns only the selected ids`() {
        addTest()
        val selected = addTest()

        mvc
            .perform(
                post("/tests/run")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "snippetId":"${snippet.id.value}",
                          "selection":{"type":"SELECTED","testIds":["${selected.value}"]}
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isAccepted)
            .andExpect(jsonPath("$.testIds.length()").value(1))
            .andExpect(jsonPath("$.testIds[0]").value(selected.value))
        assertEquals(1, publisher.published.size)
    }

    @Test
    fun `invalid selections and malformed requests return 400 without publishing`() {
        val test = addTest()
        val selections =
            listOf(
                """{"type":"SELECTED"}""",
                """{"type":"SELECTED","testIds":["${UUID.randomUUID()}"]}""",
                """{"type":"ALL","testIds":["${test.value}"]}""",
                """{"type":"INVALID"}""",
                """{"type":"SELECTED","testIds":["not-a-uuid"]}""",
            )
        selections.forEach { selection ->
            mvc
                .perform(
                    post("/tests/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"snippetId":"${snippet.id.value}","selection":$selection}"""),
                ).andExpect(status().isBadRequest)
        }
        assertEquals(0, publisher.published.size)
    }

    @Test
    fun `POST returns 403 without access and 404 for a missing snippet`() {
        SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(testJwt("stranger"))
        mvc
            .perform(
                post("/tests/run")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"snippetId":"${snippet.id.value}","selection":{"type":"ALL"}}"""),
            ).andExpect(status().isForbidden)
        mvc
            .perform(
                post("/tests/run")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"snippetId":"${UUID.randomUUID()}","selection":{"type":"ALL"}}"""),
            ).andExpect(status().isNotFound)
        assertEquals(0, publisher.published.size)
    }

    private fun addTest(): SnippetTestId {
        val id = SnippetTestId(UUID.randomUUID().toString())
        tests.save(SnippetTest(id, snippet.id, snippet.ownerId, "https://assets/tests/${id.value}"))
        return id
    }
}
