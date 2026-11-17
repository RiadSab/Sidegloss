package dev.sabti.sidegloss.text

import dev.sabti.sidegloss.Language

/**
 * Suggests the dictionary forms a word may come from, so that `ran` can be looked up as `run`.
 *
 * English uses a table of irregular forms and the spelling rules of its endings. The rules work without a
 * dictionary, so a candidate may be no word at all, such as `studie` for `studies`. No lookup matches those, so
 * they are harmless. A candidate that is a different real word is not, so the rules follow English spelling:
 * `hoping` gives `hope` and never `hop`, because `hop` is spelled `hopping`.
 */
class Lemmatizer(
    private val language: Language,
) {
    /**
     * Returns the candidate lemmas of [word], most likely first, ending with the word itself, lowercased and with
     * curly apostrophes made straight. A word no rule explains comes back alone. French returns only that form for
     * now.
     */
    fun lemmasOf(word: String): List<String> {
        val normalized = word.lowercase().replace('\u2019', '\'')
        val candidates =
            when (language) {
                Language.ENGLISH -> englishCandidates(normalized)
                Language.FRENCH -> emptyList()
            }
        return (candidates + normalized).distinct()
    }

    private fun englishCandidates(word: String): List<String> {
        val clitic = CLITICS.firstOrNull { word.length > it.length && word.endsWith(it) }
        return when {
            word in ENGLISH_WORD_FORMS -> ENGLISH_WORD_FORMS.getValue(word)
            clitic != null -> cliticCandidates(word, clitic)
            else -> suffixCandidates(word).filterNot { it in ENGLISH_FUNCTION_WORDS }
        }
    }

    // The tokenizer keeps Holmes's and doesn't whole, so the word inside is looked up: holmes, and does, which is do.
    // A few negations change the verb itself, as can't and won't do.
    private fun cliticCandidates(
        word: String,
        clitic: String,
    ): List<String> {
        val host = word.dropLast(clitic.length)
        return NEGATED_FORMS[word]?.let { listOf(it) } ?: (englishCandidates(host) + host)
    }

    private fun suffixCandidates(word: String): List<String> =
        when {
            word in UNINFLECTED_ENGLISH_WORDS -> emptyList()
            word.endsWith("ing") -> ingStemCandidates(word.dropLast(3))
            word.endsWith("est") -> comparedCandidates(word.dropLast(3))
            word.endsWith("eed") -> eedCandidates(word)
            word.endsWith("ed") -> stemCandidates(word.dropLast(2))
            word.endsWith("er") -> comparedCandidates(word.dropLast(2))
            word.endsWith('s') -> pluralCandidates(word)
            else -> emptyList()
        }

    private fun pluralCandidates(word: String): List<String> =
        when {
            word.length <= 2 || word.endsWith("ss") || word.endsWith("us") || word.endsWith("is") -> emptyList()
            word.endsWith("ies") -> iStemCandidates(word.dropLast(2))
            ES_ENDINGS.any { word.endsWith(it) } -> listOf(word.dropLast(2), word.dropLast(1))
            else -> listOf(word.dropLast(1))
        }

    // Comparison changes an adjective's form, not its meaning. Most other words ending in -er or -est are words of
    // their own (letter, corner, forest) or name a person (reader), so a base counts only if it is a known adjective.
    private fun comparedCandidates(stem: String): List<String> {
        val bases =
            when {
                stem.endsWith('i') -> listOf("${stem.dropLast(1)}y")
                stem.length >= 2 && stem.last() == stem[stem.lastIndex - 1] -> listOf(stem.dropLast(1), stem)
                else -> listOf(stem, "${stem}e")
            }
        return bases.filter { it in COMPARABLE_ENGLISH_ADJECTIVES }
    }

    // A short word ending in -eed is a word of its own, such as need, seed or speed. Only a longer one is the past
    // of a verb ending in -ee, as agreed is. The table holds freed.
    private fun eedCandidates(word: String): List<String> =
        if (word.length >= 6) listOf(word.dropLast(1)) else emptyList()

    // A final e, i, o or y stays before -ing, as in seeing, skiing, doing and studying, so such a stem is the base.
    private fun ingStemCandidates(stem: String): List<String> =
        if (stem.length >= 2 && stem.last() in KEPT_BEFORE_ING) listOf(stem) else stemCandidates(stem)

    private fun stemCandidates(stem: String): List<String> {
        if (stem.length < 2 || stem.none { it in VOWELS }) return emptyList()
        val last = stem.last()
        val doubled = stem.length > 3 && stem[stem.lastIndex - 1] == last && mayDouble(stem)
        return when {
            last == 'i' -> iStemCandidates(stem)
            doubled -> listOf(stem.dropLast(1), stem)
            needsSilentE(stem) -> listOf("${stem}e")
            else -> listOf(stem, "${stem}e")
        }
    }

    private fun iStemCandidates(stem: String): List<String> {
        val endingInY = "${stem.dropLast(1)}y"
        val endingInIe = "${stem}e"
        return if (stem.length == 2) listOf(endingInIe, endingInY) else listOf(endingInY, endingInIe)
    }

    // British spellings can also double l, as in travelling. A short stem often already ends in ll, as in call.
    private fun mayDouble(stem: String): Boolean =
        stem.last() in DOUBLED_CONSONANTS || (stem.endsWith('l') && stem.dropLast(3).any { it in VOWELS })

    // The suffix replaced an e when the stem cannot end a verb as it is. No English verb ends in u, v, dg or rg, and
    // a one-syllable verb ending in one vowel and one consonant doubles it: hop gives hopping, so hoping is hope.
    private fun needsSilentE(stem: String): Boolean =
        stem.last() in "uv" || stem.endsWith("dg") || stem.endsWith("rg") || isShortClosedSyllable(stem)

    private fun isShortClosedSyllable(stem: String): Boolean =
        stem.last() in SILENT_E_CONSONANTS &&
            stem[stem.lastIndex - 1] in VOWELS &&
            stem.dropLast(2).none { it in VOWELS }
}

/** Endings that join a word without changing it: possession, negation and contracted verbs. */
private val CLITICS = listOf("n't", "'s", "'ll", "'re", "'ve", "'m", "'d")

private val NEGATED_FORMS = mapOf("can't" to "can", "won't" to "will", "shan't" to "shall", "ain't" to "be")

private val ES_ENDINGS = listOf("ches", "shes", "sses", "xes", "zzes", "oes")

private const val VOWELS = "aeiouy"

private const val DOUBLED_CONSONANTS = "bdgmnprt"

private const val SILENT_E_CONSONANTS = "bcdfghjklmnpqrstvz"

private const val KEPT_BEFORE_ING = "aeioy"
