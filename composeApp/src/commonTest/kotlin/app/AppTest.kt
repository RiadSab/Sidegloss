package dev.sabti.sidegloss.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.sabti.sidegloss.Sidegloss
import kotlin.test.Test

class AppTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun showsNameAndTagline() =
        runComposeUiTest {
            setContent { App() }

            onNodeWithText(Sidegloss.NAME).assertExists()
            onNodeWithText(Sidegloss.TAGLINE).assertExists()
        }
}
