package dev.sabti.sidegloss.text

import dev.sabti.sidegloss.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SentenceSplitterTest {
    private val splitter = SentenceSplitter()

    /** The sentences of [paragraph] as they appear in it, spaces included. */
    private fun sentences(
        paragraph: String,
        language: Language = Language.ENGLISH,
    ): List<String> {
        val tokens = tokensOf(paragraph, language)
        return SentenceSplitter(language).split(tokens).map { range ->
            paragraph.substring(tokens[range.first].start, tokens[range.last].end)
        }
    }

    @Test
    fun splitsAfterFullStopsExclamationAndQuestionMarks() {
        assertEquals(listOf("It rained.", "We stayed in!", "Did you?"), sentences("It rained. We stayed in! Did you?"))
    }

    @Test
    fun keepsRepeatedTerminatorsTogether() {
        assertEquals(listOf("Really?!", "Yes."), sentences("Really?! Yes."))
    }

    @Test
    fun keepsEnglishTitlesWithTheNamesTheyIntroduce() {
        assertEquals(
            listOf("Mr. Holmes met Mrs. Hudson and Dr. Watson on St. James Street.", "They talked."),
            sentences("Mr. Holmes met Mrs. Hudson and Dr. Watson on St. James Street. They talked."),
        )
    }

    @Test
    fun keepsDottedAbbreviationsAndTheirExamplesInOneSentence() {
        assertEquals(
            listOf("Bring supplies, E.G. Paper, pens, etc. Plus a bag.", "We can meet here, i.e. North Hall."),
            sentences("Bring supplies, E.G. Paper, pens, etc. Plus a bag. We can meet here, i.e. North Hall."),
        )
    }

    @Test
    fun usesTheTitlesForTheTextLanguage() {
        assertEquals(
            listOf("M. Martin a vu Mme. Bernard.", "Elle est partie."),
            sentences("M. Martin a vu Mme. Bernard. Elle est partie.", Language.FRENCH),
        )
        assertEquals(listOf("Choose option M.", "Martin will help."), sentences("Choose option M. Martin will help."))
    }

    @Test
    fun splitsAtAQuestionMarkAfterAnAbbreviation() {
        assertEquals(listOf("They need pens, etc.?", "Yes."), sentences("They need pens, etc.? Yes."))
    }

    @Test
    fun keepsClosingQuoteWithItsSentence() {
        assertEquals(
            listOf("He said, \"Stay here.\"", "Then he left."),
            sentences("He said, \"Stay here.\" Then he left."),
        )
    }

    @Test
    fun leavesOpeningQuoteToTheNextSentence() {
        assertEquals(listOf("He left.", "\"Go,\" she said."), sentences("He left. \"Go,\" she said."))
    }

    @Test
    fun keepsClosingBracketWithItsSentence() {
        assertEquals(listOf("(It was cold.)", "We left."), sentences("(It was cold.) We left."))
    }

    @Test
    fun goesOnWhenTheNextWordIsLowercase() {
        assertEquals(
            listOf("“Where are you going?” he asked.", "“Help!” she cried."),
            sentences("“Where are you going?” he asked. “Help!” she cried."),
        )
    }

    @Test
    fun treatsAnEllipsisByTheWordAfterIt() {
        assertEquals(
            listOf("He waited… then he left.", "Nobody saw."),
            sentences("He waited… then he left. Nobody saw."),
        )
        assertEquals(listOf("He waited...", "Then he left."), sentences("He waited... Then he left."))
    }

    @Test
    fun keepsSpacedFrenchGuillemetsWithTheirSentence() {
        assertEquals(
            listOf("« Viens ici ! »", "Il partit."),
            sentences("« Viens ici ! » Il partit.", Language.FRENCH),
        )
    }

    @Test
    fun endsTheLastSentenceAtTheEndOfTheParagraph() {
        assertEquals(listOf("It rained.", "We stayed in"), sentences("It rained. We stayed in"))
        val tokens = tokensOf("No punctuation at all")
        assertEquals(tokens.indices, sentenceOf(tokens))
    }

    @Test
    fun returnsNoSentencesForNoTokens() {
        assertEquals(emptyList(), splitter.split(emptyList()))
    }

    @Test
    fun coversEveryTokenOnceInOrder() {
        for (paragraph in SAMPLES) {
            val tokens = tokensOf(paragraph)

            for (size in 0..tokens.size) {
                val prefix = tokens.take(size)
                val ranges = splitter.split(prefix)

                assertEquals(
                    prefix.indices.toList(),
                    ranges.flatMap { it.toList() },
                    "first $size tokens of: $paragraph",
                )
                assertTrue(ranges.none { it.isEmpty() }, "empty sentence in first $size tokens of: $paragraph")
            }
        }
    }

    private companion object {
        val SAMPLES =
            listOf(
                "",
                "...",
                "One sentence",
                "It rained. We stayed in! Did you?",
                "He said, \"Stay here.\" Then he left. \"Why?\" she asked.",
                "“Help!” she cried… Nobody came?! (Odd.)",
                "Mr. Holmes met Dr. Watson at 3.30. They talked.",
                "Use a pencil, e.g. Any one from the box. There are plenty.",
            )
    }
}
