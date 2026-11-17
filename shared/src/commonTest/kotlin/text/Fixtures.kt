package dev.sabti.sidegloss.text

import dev.sabti.sidegloss.Language

/** Tokenizes a test paragraph, using English by default. */
internal fun tokensOf(
    paragraph: String,
    language: Language = Language.ENGLISH,
): List<Token> = Tokenizer(language).tokenize(paragraph)

/** Finds the one-based [occurrence] of the exact [word], counting only word tokens. */
internal fun List<Token>.indexOfWord(
    word: String,
    occurrence: Int = 1,
): Int {
    val matches =
        indices.filter { index ->
            val token = this[index]
            token.kind == TokenKind.WORD && token.text == word
        }
    return requireNotNull(matches.getOrNull(occurrence - 1)) {
        "Expected occurrence $occurrence of '$word' in ${map { it.text }}"
    }
}

/** Returns the range in [tokens] for a fixture that contains exactly one sentence. */
internal fun sentenceOf(
    tokens: List<Token>,
    language: Language = Language.ENGLISH,
): IntRange = SentenceSplitter(language).split(tokens).single()
