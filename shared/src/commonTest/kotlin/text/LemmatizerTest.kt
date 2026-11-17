package dev.sabti.sidegloss.text

import dev.sabti.sidegloss.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
                "rose" to "rise",
                "led" to "lead",
                "lay" to "lie",
                "fought" to "fight",
                "grew" to "grow",
                "dying" to "die",
                "halves" to "half",
                "more" to "much",
                "further" to "far",
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
        assertEquals(listOf("good", "well", "better"), english.lemmasOf("better"))
        assertEquals(listOf("leaf", "leave", "leaves"), english.lemmasOf("leaves"))
    }

    @Test
    fun followsEnglishSpellingSoNoOtherWordIsSuggested() {
        val cases =
            mapOf(
                "hoping" to listOf("hope", "hoping"),
                "hoped" to listOf("hope", "hoped"),
                "staring" to listOf("stare", "staring"),
                "noted" to listOf("note", "noted"),
                "planing" to listOf("plane", "planing"),
                "having" to listOf("have", "having"),
                "arguing" to listOf("argue", "arguing"),
                "judged" to listOf("judge", "judged"),
                "seeing" to listOf("see", "seeing"),
                "skiing" to listOf("ski", "skiing"),
                "doing" to listOf("do", "doing"),
                "studying" to listOf("study", "studying"),
                "agreed" to listOf("agree", "agreed"),
                "freed" to listOf("free", "freed"),
            )

        for ((word, lemmas) in cases) {
            assertEquals(lemmas, english.lemmasOf(word), word)
        }
    }

    @Test
    fun undoesADoubledConsonant() {
        assertEquals("hop", english.lemmasOf("hopping").first())
        assertEquals("star", english.lemmasOf("starring").first())
        assertEquals("plan", english.lemmasOf("planned").first())
    }

    @Test
    fun comparesOnlyAdjectives() {
        val wordsOfTheirOwn =
            listOf("letter", "butter", "corner", "reader", "teacher", "cover", "ever", "mother", "forest", "honest")

        for (word in wordsOfTheirOwn) {
            assertEquals(listOf(word), english.lemmasOf(word))
        }
        assertEquals(listOf("free", "freer"), english.lemmasOf("freer"))
        assertEquals(listOf("late", "later"), english.lemmasOf("later"))
        assertEquals(listOf("silly", "sillier"), english.lemmasOf("sillier"))
        assertEquals(listOf("tall", "taller"), english.lemmasOf("taller"))
    }

    @Test
    fun putsTheNounFirstWhenItIsTheCommonerReading() {
        assertEquals(listOf("stranger", "strange"), english.lemmasOf("stranger"))
    }

    @Test
    fun looksInsidePossessivesAndContractions() {
        val cases =
            mapOf(
                "Holmes's" to "holmes",
                "children's" to "child",
                "Alice’s" to "alice",
                "don't" to "do",
                "doesn't" to "do",
                "isn't" to "be",
                "can't" to "can",
                "won't" to "will",
                "let's" to "let",
                "they'll" to "they",
            )

        for ((word, lemma) in cases) {
            val lemmas = english.lemmasOf(word)

            assertTrue(lemma in lemmas, "$word gave $lemmas")
            assertEquals(word.lowercase().replace('\u2019', '\''), lemmas.last(), word)
        }
        assertEquals(listOf("child", "children", "children's"), english.lemmasOf("children's"))
        assertEquals(listOf("do", "does", "doesn't"), english.lemmasOf("doesn't"))
    }

    @Test
    fun leavesWordsWithAnInnerApostropheAlone() {
        for (word in listOf("o'clock", "ma'am", "rock'n'roll")) {
            assertEquals(listOf(word), english.lemmasOf(word))
        }
    }

    @Test
    fun leavesShortWordsEndingInEedWhole() {
        for (word in listOf("need", "seed", "feed", "weed", "speed")) {
            assertEquals(listOf(word), english.lemmasOf(word))
        }
    }

    @Test
    fun neverDerivesFunctionWordsFromASuffix() {
        val falseMatches =
            mapOf("offer" to "off", "outer" to "out", "butter" to "but", "forest" to "for", "its" to "it")

        for ((word, functionWord) in falseMatches) {
            assertFalse(functionWord in english.lemmasOf(word), word)
        }
        assertEquals(listOf("use", "used"), english.lemmasOf("used"))
    }

    @Test
    fun leavesWordsThatOnlyLookInflectedAlone() {
        for (word in listOf("news", "always", "evening", "nothing", "wicked", "earring", "clothes", "yes")) {
            assertEquals(listOf(word), english.lemmasOf(word))
        }
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
