package dev.sabti.sidegloss.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TokenizerTest {
    private val tokenizer = Tokenizer()

    private fun texts(paragraph: String) = tokenizer.tokenize(paragraph).map { it.text }

    @Test
    fun offsetsSelectEachTokenText() {
        for (paragraph in SAMPLES) {
            for (token in tokenizer.tokenize(paragraph)) {
                assertEquals(token.text, paragraph.substring(token.start, token.end), "in: $paragraph")
            }
        }
    }

    @Test
    fun offsetsIncreaseWithoutOverlap() {
        for (paragraph in SAMPLES) {
            val tokens = tokenizer.tokenize(paragraph)
            for ((previous, next) in tokens.zipWithNext()) {
                assertTrue(previous.end <= next.start, "$previous overlaps $next in: $paragraph")
            }
            assertTrue(tokens.all { it.start < it.end }, "empty token in: $paragraph")
        }
    }

    @Test
    fun tokensCoverEveryCharacterExceptWhitespace() {
        for (paragraph in SAMPLES) {
            val joined = tokenizer.tokenize(paragraph).joinToString("") { it.text }

            assertEquals(paragraph.filterNot { it.isWhitespace() }, joined)
        }
    }

    @Test
    fun keepsContractionsWhole() {
        assertEquals(listOf("I", "don't", "think", "they'll", "come", "."), texts("I don't think they'll come."))
    }

    @Test
    fun keepsHyphenatedWordsWhole() {
        assertEquals(
            listOf("A", "well-known", ",", "old-fashioned", "tale"),
            texts("A well-known, old-fashioned tale"),
        )
    }

    @Test
    fun treatsOuterApostrophesAndHyphensAsPunctuation() {
        assertEquals(listOf("'", "Tis", "the", "dogs", "'", "-", "bone", "-"), texts("'Tis the dogs' -bone-"))
        assertEquals(listOf("well", "-", "-", "known"), texts("well--known"))
    }

    @Test
    fun separatesCurlyQuotesAndKeepsCurlyApostrophes() {
        assertEquals(
            listOf("“", "Don’t", "go", ",", "”", "she", "said", "."),
            texts("“Don’t go,” she said."),
        )
    }

    @Test
    fun makesEmDashAToken() {
        assertEquals(listOf("Wait", "—", "what", "?"), texts("Wait—what?"))
    }

    @Test
    fun keepsDecimalAndGroupedNumbersWhole() {
        val tokens = tokenizer.tokenize("It costs 3.5 or 1,000. Chapter 3.")

        assertEquals(listOf("It", "costs", "3.5", "or", "1,000", ".", "Chapter", "3", "."), tokens.map { it.text })
        assertEquals(TokenKind.NUMBER, tokens[2].kind)
        assertEquals(TokenKind.NUMBER, tokens[4].kind)
        assertEquals(TokenKind.PUNCTUATION, tokens[5].kind)
    }

    @Test
    fun returnsNoTokensForEmptyOrBlankText() {
        assertEquals(emptyList(), tokenizer.tokenize(""))
        assertEquals(emptyList(), tokenizer.tokenize("  \t   "))
    }

    @Test
    fun keepsCombiningAccentInsideWord() {
        assertEquals(listOf("café", "noir"), texts("café noir"))
    }

    @Test
    fun keepsEmojiAsOnePunctuationToken() {
        val tokens = tokenizer.tokenize("Nice 👍 work")

        assertEquals(listOf("Nice", "👍", "work"), tokens.map { it.text })
        assertEquals(TokenKind.PUNCTUATION, tokens[1].kind)
    }

    private companion object {
        val SAMPLES =
            listOf(
                "",
                "   ",
                "I don't think they'll come.",
                "“Don’t go,” she said—quietly—and left at 3.30, not 4.",
                "'Tis the dogs' well-known -bone- day... Isn't it?",
                "It costs 1,000.50 (or so) in 2026!",
                "Nice 👍 work, café owner.",
            )
    }
}
