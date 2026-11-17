package dev.sabti.sidegloss.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.sabti.sidegloss.Sidegloss
import dev.sabti.sidegloss.app.App

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = Sidegloss.NAME,
        ) {
            App()
        }
    }
