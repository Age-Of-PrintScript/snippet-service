package age.of.printscript.demo.shared.infrastructure.configuration

import age.of.printscript.demo.snippet.application.CreateSnippet
import age.of.printscript.demo.snippet.application.FakeLanguage
import age.of.printscript.demo.snippet.application.FakePermissionGateway
import age.of.printscript.demo.snippet.application.FakeSnippetStorage
import age.of.printscript.demo.snippet.application.GetSnippet
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.snippet.infrastructure.controller.SnippetController
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringJUnitWebConfig(SecurityConfigurationTest.TestConfig::class)
class SecurityConfigurationTest {
    @Configuration
    @EnableWebMvc
    @Import(SecurityConfiguration::class, SnippetController::class)
    class TestConfig {
        // Reemplaza al decoder real, que necesitaría contactar a Auth0 para obtener el JWKS.
        @Bean
        fun jwtDecoder(): JwtDecoder = mock(JwtDecoder::class.java)

        @Bean
        fun getSnippet(): GetSnippet {
            val snippet = createDummySnippet(ownerId = UserId("auth0|alice"))
            val storage = FakeSnippetStorage().apply { set(snippet.contentUrl, "let a: number = 5;") }
            return GetSnippet(InMemorySnippetRepository(listOf(snippet)), storage, FakePermissionGateway())
        }

        @Bean
        fun createSnippet(): CreateSnippet {
            val languages = listOf(FakeLanguage())
            return CreateSnippet(InMemorySnippetRepository(), FakeSnippetStorage(), languages)
        }
    }

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var jwtDecoder: JwtDecoder

    private lateinit var mockMvc: MockMvc

    private val createBody =
        """{"title":"t","description":null,"language":"printscript","languageVersion":"1.1","content":"x"}"""

    @BeforeEach
    fun setUp() {
        reset(jwtDecoder)
        mockMvc =
            MockMvcBuilders
                .webAppContextSetup(context)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()
    }

    @Test
    fun `GET snippet without token returns 401`() {
        mockMvc.perform(get("/snippet/any")).andExpect(status().isUnauthorized)
    }

    @Test
    fun `POST snippet without token returns 401`() {
        mockMvc
            .perform(
                post("/snippet")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createBody),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `any other path without token returns 401 instead of 404`() {
        mockMvc.perform(get("/does-not-exist")).andExpect(status().isUnauthorized)
    }

    @Test
    fun `invalid bearer token returns 401`() {
        `when`(jwtDecoder.decode("garbage")).thenThrow(BadJwtException("bad"))

        mockMvc
            .perform(get("/snippet/any").header("Authorization", "Bearer garbage"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `valid jwt reaches the controller`() {
        mockMvc
            .perform(get("/snippet/missing").with(jwt()))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `controller uses the jwt subject as the user id`() {
        val snippetId = createDummySnippet(ownerId = UserId("auth0|alice")).id.value

        mockMvc
            .perform(get("/snippet/$snippetId").with(jwt().jwt { it.subject("auth0|alice") }))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ownerId").value("auth0|alice"))
    }
}
