package dev.sabti.sidegloss.text

import kotlin.test.Test
import kotlin.test.assertFailsWith

class TextIdTest {
    @Test
    fun rejectsBlankValue() {
        assertFailsWith<IllegalArgumentException> { TextId("") }
        assertFailsWith<IllegalArgumentException> { TextId("  ") }
    }
}
