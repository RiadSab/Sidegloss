package dev.sabti.sidegloss

import kotlinx.serialization.json.Json
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

    @Test
    fun jsonFormIsTheTag() {
        for (language in Language.entries) {
            val json = Json.encodeToString(Language.serializer(), language)

            assertEquals("\"${language.tag}\"", json)
            assertEquals(language, Json.decodeFromString(Language.serializer(), json))
        }
    }
}
