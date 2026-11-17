package dev.sabti.sidegloss.text

import dev.sabti.sidegloss.Language

/** Suggests dictionary forms for a word in the selected language. */
class Lemmatizer(
    private val language: Language,
) {
    /**
     * Returns the likely dictionary forms of [word] in lookup order, with the lowercased word as a fallback.
     * English uses irregular forms and suffix rules. French returns just the lowercased word.
     */
    fun lemmasOf(word: String): List<String> {
        val lowercased = word.lowercase()
        return when (language) {
            Language.ENGLISH -> {
                val candidates = ENGLISH_WORD_FORMS[lowercased] ?: englishCandidates(lowercased)
                (candidates + lowercased).distinct()
            }

            Language.FRENCH -> {
                listOf(lowercased)
            }
        }
    }

    private fun englishCandidates(word: String): List<String> =
        when {
            word.endsWith("ing") -> stemCandidates(word.dropLast(3))
            word.endsWith("est") -> stemCandidates(word.dropLast(3))
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

    private fun stemCandidates(stem: String): List<String> {
        if (stem.length < 2 || stem.none { it in VOWELS }) return emptyList()
        val last = stem.last()
        val doubled = stem.length > 3 && stem[stem.lastIndex - 1] == last && mayDouble(stem)
        return when {
            last == 'i' -> iStemCandidates(stem)
            doubled -> listOf(stem.dropLast(1), stem)
            prefersSilentE(stem) -> listOf("${stem}e", stem)
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

    // A short stem ending in a vowel and a consonant often lost an e, as in hoping or used.
    private fun prefersSilentE(stem: String): Boolean =
        when {
            stem.endsWith('v') || stem.endsWith("dg") || stem.endsWith("rg") -> {
                true
            }

            stem.last() in SILENT_E_CONSONANTS && stem[stem.lastIndex - 1] in VOWELS -> {
                stem.dropLast(2).none { it in VOWELS }
            }

            else -> {
                false
            }
        }
}

private val ES_ENDINGS = listOf("ches", "shes", "sses", "xes", "zzes", "oes")

private const val VOWELS = "aeiouy"

private const val DOUBLED_CONSONANTS = "bdgmnprt"

private const val SILENT_E_CONSONANTS = "bcdfghjklmnpqrstvz"
