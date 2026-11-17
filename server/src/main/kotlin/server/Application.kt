package dev.sabti.sidegloss.server

import dev.sabti.sidegloss.Sidegloss
import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/** Port used when the `PORT` environment variable is not set. The web dev server owns 8080. */
private const val DEFAULT_PORT = 8081

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: DEFAULT_PORT
    embeddedServer(CIO, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    routing {
        get("/") {
            call.respondText("${Sidegloss.NAME} server is running")
        }
        get("/health") {
            call.respondText("ok")
        }
    }
}
