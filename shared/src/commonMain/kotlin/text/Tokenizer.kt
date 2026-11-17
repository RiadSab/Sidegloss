package dev.sabti.sidegloss.text

/**
 * Splits a paragraph into words, numbers and punctuation, keeping the character offsets of each piece so a tap on
 * the screen can be traced back to a token and a token back to its place in the text.
 *
 * A word is a run of letters that may contain an apostrophe or hyphen between two letters, so `don't` and
 * `well-known` stay whole while a leading or trailing apostrophe or hyphen is punctuation. A number is a run of
 * digits that may contain a `.` or `,` between two digits, such as `3.5` or `1,000`. Every other character that is
 * not whitespace is a punctuation token of its own.
 */
class Tokenizer {
    /** Returns the tokens of [paragraph] in reading order. Whitespace produces no token. */
    fun tokenize(paragraph: String): List<Token> =
        buildList {
            var start = 0
            while (start < paragraph.length) {
                val char = paragraph[start]
                if (char.isWhitespace()) {
                    start++
                    continue
                }
                val kind = kindStartingWith(char)
                val end =
                    when (kind) {
                        TokenKind.WORD -> wordEnd(paragraph, start)
                        TokenKind.NUMBER -> numberEnd(paragraph, start)
                        TokenKind.PUNCTUATION -> punctuationEnd(paragraph, start)
                    }
                add(Token(text = paragraph.substring(start, end), start = start, end = end, kind = kind))
                start = end
            }
        }

    private fun kindStartingWith(char: Char): TokenKind =
        when {
            char.isLetter() -> TokenKind.WORD
            char.isDigit() -> TokenKind.NUMBER
            else -> TokenKind.PUNCTUATION
        }

    private fun wordEnd(
        text: String,
        start: Int,
    ): Int {
        var end = start + 1
        while (end < text.length) {
            val char = text[end]
            when {
                char.isWordContinuation() -> end++
                char in WORD_JOINERS && text.getOrNull(end + 1)?.isLetter() == true -> end += 2
                else -> return end
            }
        }
        return end
    }

    private fun numberEnd(
        text: String,
        start: Int,
    ): Int {
        var end = start + 1
        while (end < text.length) {
            val char = text[end]
            when {
                char.isDigit() -> end++
                char in NUMBER_SEPARATORS && text.getOrNull(end + 1)?.isDigit() == true -> end += 2
                else -> return end
            }
        }
        return end
    }

    /** A character outside the Basic Multilingual Plane, such as most emoji, is one token, not two halves. */
    private fun punctuationEnd(
        text: String,
        start: Int,
    ): Int {
        val isSurrogatePair = text[start].isHighSurrogate() && text.getOrNull(start + 1)?.isLowSurrogate() == true
        return if (isSurrogatePair) start + 2 else start + 1
    }
}

/** Letters, and the combining accents that some texts store after a letter instead of inside it. */
private fun Char.isWordContinuation() =
    isLetter() || category == CharCategory.NON_SPACING_MARK || category == CharCategory.COMBINING_SPACING_MARK

/** The straight and the curly apostrophe. The curly one doubles as the closing single quote. */
private val APOSTROPHES = setOf('\'', '\u2019')

/** Hyphen-minus, hyphen and non-breaking hyphen. */
private val HYPHENS = setOf('-', '\u2010', '\u2011')

private val WORD_JOINERS = APOSTROPHES + HYPHENS

private val NUMBER_SEPARATORS = setOf('.', ',')
