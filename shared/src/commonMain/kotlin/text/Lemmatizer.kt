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
     * Returns the candidate lemmas of [word], most likely first, ending with the lowercased word itself. A word no
     * rule explains comes back alone. French returns only the lowercased word for now.
     */
    fun lemmasOf(word: String): List<String> {
        val lowercased = word.lowercase()
        val candidates =
            when (language) {
                Language.ENGLISH -> englishCandidates(lowercased)
                Language.FRENCH -> emptyList()
            }
        return (candidates + lowercased).distinct()
    }

    private fun englishCandidates(word: String): List<String> =
        ENGLISH_WORD_FORMS[word] ?: suffixCandidates(word).filterNot { it in ENGLISH_FUNCTION_WORDS }

    private fun suffixCandidates(word: String): List<String> =
        when {
            word in UNINFLECTED_ENGLISH_WORDS -> emptyList()
            word.endsWith("ing") -> ingStemCandidates(word.dropLast(3))
            word.endsWith("est") -> stemCandidates(word.dropLast(3))
            word.endsWith("eed") -> eedCandidates(word)
            word.endsWith("ed") -> stemCandidates(word.dropLast(2))
            word.endsWith("er") -> stemCandidates(word.dropLast(2))
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

private val ES_ENDINGS = listOf("ches", "shes", "sses", "xes", "zzes", "oes")

private const val VOWELS = "aeiouy"

private const val DOUBLED_CONSONANTS = "bdgmnprt"

private const val SILENT_E_CONSONANTS = "bcdfghjklmnpqrstvz"

private const val KEPT_BEFORE_ING = "aeioy"
