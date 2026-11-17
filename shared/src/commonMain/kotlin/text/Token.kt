package dev.sabti.sidegloss.text

/**
 * One piece of a paragraph: its [text], what [kind] of piece it is, and where it sits in the paragraph.
 *
 * [start] and [end] are character offsets into the paragraph. [start] is inclusive and [end] is exclusive, so
 * `paragraph.substring(start, end)` is [text].
 */
data class Token(
    val text: String,
    val start: Int,
    val end: Int,
    val kind: TokenKind,
) {
    init {
        require(start >= 0) { "start must not be negative, was $start" }
        require(start <= end) { "start ($start) must not be after end ($end)" }
        require(text.length == end - start) {
            "text has ${text.length} characters but the offsets span ${end - start}"
        }
    }
}

/** What a [Token] is made of. */
enum class TokenKind {
    /** Letters, possibly joined by an inner apostrophe or hyphen, such as `don't` or `well-known`. */
    WORD,

    /** Digits, possibly joined by an inner `.` or `,`, such as `3.5` or `1,000`. */
    NUMBER,

    /** Any other character that is not whitespace. */
    PUNCTUATION,
}
