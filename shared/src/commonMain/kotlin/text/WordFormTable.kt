package dev.sabti.sidegloss.text

/**
 * The inflected forms of about 47,000 English words and the base words they come from, so the dictionary form of a
 * word is looked up instead of guessed. The data is derived from AGID; `shared/data/english-word-forms.txt`
 * describes the format and `shared/data/AGID-README.txt` holds its copyright notice.
 *
 * Two kinds of entry are left out because they point to a different word. Function words are skipped, since AGID
 * gives `its` as a form of `it` and `upper` as a form of `up`. A comparison that is a word of its own is skipped
 * unless its base is a known comparable adjective: AGID gives `butter` for `but` and `layer` for `lay`, but
 * `cleaner` still leads to `clean`.
 */
internal class WordFormTable(
    lines: Iterable<String>,
) {
    private val bases: Set<String>
    private val lemmasByForm: Map<String, List<String>>

    init {
        val entries =
            lines
                .filter { it.isNotBlank() && !it.startsWith('#') }
                .map(::parseEntry)
                .filter { it.base !in ENGLISH_FUNCTION_WORDS }
        bases = entries.mapTo(HashSet()) { it.base }

        val lemmas = HashMap<String, MutableList<String>>()
        for (entry in entries) {
            val isComparison = entry.partOfSpeech == 'A'
            for (form in entry.forms) {
                val isWordOfItsOwn = form in bases && entry.base !in COMPARABLE_ENGLISH_ADJECTIVES
                if (isComparison && isWordOfItsOwn) continue
                val formLemmas = lemmas.getOrPut(form) { mutableListOf() }
                if (entry.base !in formLemmas) formLemmas += entry.base
            }
        }
        lemmasByForm = lemmas
    }

    /** Whether [word] is in the table, as a base word or as an inflected form. */
    operator fun contains(word: String): Boolean = word in bases || word in lemmasByForm

    /** Whether [word] is a base word, one that dictionaries list. */
    fun isBase(word: String): Boolean = word in bases

    /** Returns the base words [form] is an inflection of, in the order of the data, or an empty list. */
    fun lemmasOf(form: String): List<String> = lemmasByForm[form].orEmpty()

    private class Entry(
        val partOfSpeech: Char,
        val base: String,
        val forms: List<String>,
    )

    private fun parseEntry(line: String): Entry {
        val parts = line.split(' ')
        require(parts.size >= 2 && parts[0].length == 1) { "Unexpected word-form line: $line" }
        val base = parts[1]
        val forms = parts.drop(2).map { if (it.startsWith('+')) base + it.drop(1) else it }
        return Entry(partOfSpeech = parts[0].single(), base = base, forms = forms)
    }

    companion object {
        /**
         * The table built from the word-form data compiled into the app, on first use. Building it takes tens of
         * milliseconds on a desktop and more on a phone, so an app should touch it in the background at start.
         */
        val english: WordFormTable by lazy {
            WordFormTable(ENGLISH_WORD_FORM_DATA.flatMap { it.split('\n') })
        }
    }
}
