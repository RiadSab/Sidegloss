package dev.sabti.sidegloss

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LanguageTest {
    @Test
    fun fromTagMatchesPrimarySubtag() {
        assertEquals(Language.ENGLISH, Language.fromTag("en"))
        assertEquals(Language.FRENCH, Language.fromTag("fr"))
    }

    @Test
    fun fromTagIgnoresCaseAndRegion() {
        assertEquals(Language.ENGLISH, Language.fromTag("EN-us"))
        assertEquals(Language.FRENCH, Language.fromTag("fr_MA"))
    }

    @Test
    fun fromTagReturnsNullForUnsupportedLanguage() {
        assertNull(Language.fromTag("de"))
        assertNull(Language.fromTag(""))
    }
}
