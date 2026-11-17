package dev.sabti.sidegloss.server

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    @Test
    fun healthRouteAnswersOk() =
        testApplication {
            application { module() }

            val response = client.get("/health")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("ok", response.bodyAsText())
        }

    @Test
    fun rootRouteNamesTheApplication() =
        testApplication {
            application { module() }

            val response = client.get("/")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue("Sidegloss" in response.bodyAsText())
        }
}
