package dev.sabti.sidegloss.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.sabti.sidegloss.app.App
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = checkNotNull(document.body) { "The page has no body element to draw into." }
    ComposeViewport(body) {
        App()
    }
}
