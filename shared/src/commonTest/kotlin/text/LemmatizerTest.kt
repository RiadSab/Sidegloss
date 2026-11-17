package dev.sabti.sidegloss.text

import dev.sabti.sidegloss.Language
import kotlin.test.Test
import kotlin.test.assertEquals

class LemmatizerTest {
    private val english = Lemmatizer(Language.ENGLISH)

    @Test
    fun ranksEnglishDictionaryFormsFirst() {
        val cases =
            mapOf(
                "putting" to "put",
                "ran" to "run",
                "studies" to "study",
                "made" to "make",
                "Children" to "child",
                "stopped" to "stop",
                "hoping" to "hope",
                "better" to "good",
                "walks" to "walk",
                "wishes" to "wish",
                "ties" to "tie",
                "studied" to "study",
                "walked" to "walk",
                "jumping" to "jump",
                "travelling" to "travel",
                "calling" to "call",
                "wider" to "wide",
                "largest" to "large",
                "faster" to "fast",
                "biggest" to "big",
                "happier" to "happy",
                "happiest" to "happy",
            )

        for ((word, lemma) in cases) {
            val candidates = english.lemmasOf(word)

            assertEquals(lemma, candidates.first(), word)
            assertEquals(word.lowercase(), candidates.last(), word)
            assertEquals(candidates.distinct(), candidates, word)
        }
    }

    @Test
    fun keepsAlternativeCandidatesForAmbiguousForms() {
        assertEquals(listOf("hope", "hop", "hoping"), english.lemmasOf("hoping"))
        assertEquals(listOf("good", "well", "better"), english.lemmasOf("better"))
        assertEquals(listOf("leaf", "leave", "leaves"), english.lemmasOf("leaves"))
    }

    @Test
    fun keepsEnglishWordsWithoutApplicableRules() {
        for (word in listOf("Lantern", "put", "glass", "bring", "")) {
            assertEquals(listOf(word.lowercase()), english.lemmasOf(word))
        }
    }

    @Test
    fun lowercasesFrenchWithoutApplyingEnglishRules() {
        val french = Lemmatizer(Language.FRENCH)

        assertEquals(listOf("études"), french.lemmasOf("Études"))
        assertEquals(listOf("aimer"), french.lemmasOf("AIMER"))
    }
}
