package dev.sabti.sidegloss.text

import dev.sabti.sidegloss.Language

/**
 * Finds the sentences of a paragraph from its tokens.
 *
 * A sentence ends after `.`, `!`, `?` or `…`, together with any terminators and closing quotes or brackets that
 * follow. Periods in known abbreviations are ignored. A sentence also continues when the next word starts with
 * a lowercase letter, as in `“Help!” she cried.`
 */
class SentenceSplitter(
    language: Language = Language.ENGLISH,
) {
    private val abbreviations =
        when (language) {
            Language.ENGLISH -> ENGLISH_ABBREVIATIONS
            Language.FRENCH -> FRENCH_ABBREVIATIONS
        }

    /**
     * Returns the sentences of a paragraph as ranges of indices into its [tokens]. The ranges are in order, never
     * empty, and together cover every token exactly once.
     */
    fun split(tokens: List<Token>): List<IntRange> =
        buildList {
            var start = 0
            var index = 0
            while (index < tokens.size) {
                if (tokens[index].isTerminator() && !isAbbreviationPeriod(tokens, index)) {
                    val end = boundaryEnd(tokens, index)
                    if (!continuesInLowercase(tokens, end)) {
                        add(start..end)
                        start = end + 1
                    }
                    index = end + 1
                } else {
                    index++
                }
            }
            if (start < tokens.size) add(start..tokens.lastIndex)
        }

    private fun isAbbreviationPeriod(
        tokens: List<Token>,
        period: Int,
    ): Boolean {
        if (tokens[period].text != ".") return false
        val previous = tokens.getOrNull(period - 1) ?: return false
        if (previous.kind != TokenKind.WORD || previous.end != tokens[period].start) return false

        return previous.text.lowercase() in abbreviations ||
            isDottedAbbreviationAt(tokens, period - 1) ||
            isDottedAbbreviationAt(tokens, period - 3)
    }

    // The tokenizer gives e.g. four tokens. Either period may be the one being checked.
    private fun isDottedAbbreviationAt(
        tokens: List<Token>,
        start: Int,
    ): Boolean {
        if (start < 0 || start + 3 >= tokens.size) return false
        val first = tokens[start]
        val firstPeriod = tokens[start + 1]
        val second = tokens[start + 2]
        val lastPeriod = tokens[start + 3]
        return firstPeriod.text == "." && lastPeriod.text == "." &&
            first.end == firstPeriod.start && firstPeriod.end == second.start && second.end == lastPeriod.start &&
            "${first.text}.${second.text}".lowercase() in abbreviations
    }

    /** Extends the boundary that starts at [terminator] over the terminators and closers right after it. */
    private fun boundaryEnd(
        tokens: List<Token>,
        terminator: Int,
    ): Int {
        var end = terminator
        while (end < tokens.lastIndex && tokens[end + 1].continuesBoundaryAfter(tokens[end])) end++
        return end
    }

    private fun continuesInLowercase(
        tokens: List<Token>,
        boundaryEnd: Int,
    ): Boolean {
        val next = tokens.subList(boundaryEnd + 1, tokens.size).firstOrNull { it.kind != TokenKind.PUNCTUATION }
        return next?.kind == TokenKind.WORD && next.text.firstOrNull()?.isLowerCase() == true
    }
}

private fun Token.isTerminator() = text in TERMINATORS

/**
 * Whether this token still belongs to the sentence end that [previous] is part of. A straight quote can open the
 * next sentence as well as close this one, so it belongs here only when no space separates it from [previous].
 */
private fun Token.continuesBoundaryAfter(previous: Token): Boolean =
    when (text) {
        in TERMINATORS, in CLOSERS -> true
        in STRAIGHT_QUOTES -> start == previous.end
        else -> false
    }

private val TERMINATORS = setOf(".", "!", "?", "\u2026")

/** Brackets, and the quotes that can only close: right single and double quotes, right guillemets. */
private val CLOSERS = setOf(")", "]", "}", "\u2019", "\u201d", "\u203a", "\u00bb")

private val STRAIGHT_QUOTES = setOf("\"", "'")

private val ENGLISH_ABBREVIATIONS = setOf("mr", "mrs", "ms", "dr", "st", "e.g", "i.e", "etc")

private val FRENCH_ABBREVIATIONS = setOf("m", "mme", "mlle", "dr", "etc")
