package dev.sabti.sidegloss.text

import dev.sabti.sidegloss.Language
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class DocumentTest {
    private val document =
        Document(
            id = TextId("lighthouse"),
            title = "The Lighthouse",
            language = Language.ENGLISH,
            paragraphs =
                listOf(
                    "The keeper didn't put up with the storm.",
                    "He turned the lamp off at dawn.",
                ),
        )

    @Test
    fun jsonRoundTripGivesAnEqualDocument() {
        val json = Json.encodeToString(Document.serializer(), document)

        assertEquals(document, Json.decodeFromString(Document.serializer(), json))
    }

    @Test
    fun jsonUsesPlainStringsForIdAndLanguage() {
        val expected =
            """{"id":"lighthouse","title":"The Lighthouse","language":"en",""" +
                """"paragraphs":["The keeper didn't put up with the storm.","He turned the lamp off at dawn."]}"""

        assertEquals(expected, Json.encodeToString(Document.serializer(), document))
    }
}
