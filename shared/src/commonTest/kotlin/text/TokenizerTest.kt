package dev.sabti.sidegloss.text

import dev.sabti.sidegloss.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TokenizerTest {
    private val allTokenizers = Language.entries.map { Tokenizer(it) }

    private fun texts(
        paragraph: String,
        language: Language = Language.ENGLISH,
    ) = tokensOf(paragraph, language).map { it.text }

    @Test
    fun offsetsSelectEachTokenText() {
        for (tokenizer in allTokenizers) {
            for (paragraph in SAMPLES) {
                for (token in tokenizer.tokenize(paragraph)) {
                    assertEquals(token.text, paragraph.substring(token.start, token.end), "in: $paragraph")
                }
            }
        }
    }

    @Test
    fun offsetsIncreaseWithoutOverlap() {
        for (tokenizer in allTokenizers) {
            for (paragraph in SAMPLES) {
                val tokens = tokenizer.tokenize(paragraph)
                for ((previous, next) in tokens.zipWithNext()) {
                    assertTrue(previous.end <= next.start, "$previous overlaps $next in: $paragraph")
                }
                assertTrue(tokens.all { it.start < it.end }, "empty token in: $paragraph")
            }
        }
    }

    @Test
    fun tokensCoverEveryCharacterExceptWhitespace() {
        for (tokenizer in allTokenizers) {
            for (paragraph in SAMPLES) {
                val joined = tokenizer.tokenize(paragraph).joinToString("") { it.text }

                assertEquals(paragraph.filterNot { it.isWhitespace() }, joined)
            }
        }
    }

    @Test
    fun keepsContractionsWhole() {
        val tokens = tokensOf("I don't think they'll come. I don't mind.")

        assertEquals(
            listOf("I", "don't", "think", "they'll", "come", ".", "I", "don't", "mind", "."),
            tokens.map { it.text },
        )
        assertEquals(1, tokens.indexOfWord("don't"))
        assertEquals(7, tokens.indexOfWord("don't", occurrence = 2))
    }

    @Test
    fun splitsFrenchElisionsAfterTheApostrophe() {
        assertEquals(
            listOf("L'", "homme", "qu'", "il", "aime", "vient", "aujourd'hui", "jusqu'", "à", "midi", "."),
            texts("L'homme qu'il aime vient aujourd'hui jusqu'à midi.", Language.FRENCH),
        )
        assertEquals(listOf("l’", "arbre", "d’", "à", "côté"), texts("l’arbre d’à côté", Language.FRENCH))
    }

    @Test
    fun keepsFrenchWordsWrittenWithAnApostropheWhole() {
        assertEquals(listOf("quelqu'un", "d'", "autre"), texts("quelqu'un d'autre", Language.FRENCH))
    }

    @Test
    fun keepsElisionLikeEnglishWordsWhole() {
        assertEquals(listOf("l'homme", "o'clock"), texts("l'homme o'clock"))
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
        val tokens = tokensOf("It costs 3.5 or 1,000. Chapter 3.")

        assertEquals(listOf("It", "costs", "3.5", "or", "1,000", ".", "Chapter", "3", "."), tokens.map { it.text })
        assertEquals(TokenKind.NUMBER, tokens[2].kind)
        assertEquals(TokenKind.NUMBER, tokens[4].kind)
        assertEquals(TokenKind.PUNCTUATION, tokens[5].kind)
    }

    @Test
    fun returnsNoTokensForEmptyOrBlankText() {
        assertEquals(emptyList(), tokensOf(""))
        assertEquals(emptyList(), tokensOf("  \t \u00a0 "))
    }

    @Test
    fun keepsCombiningAccentInsideWord() {
        assertEquals(listOf("cafe\u0301", "noir"), texts("cafe\u0301 noir"))
    }

    @Test
    fun keepsEmojiAsOnePunctuationToken() {
        val tokens = tokensOf("Nice \uD83D\uDC4D work")

        assertEquals(listOf("Nice", "\uD83D\uDC4D", "work"), tokens.map { it.text })
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
                "Nice \uD83D\uDC4D work, cafe\u0301 owner.",
                "« L'homme qu'il aime ? » demanda-t-elle, aujourd'hui.",
            )
    }
}
