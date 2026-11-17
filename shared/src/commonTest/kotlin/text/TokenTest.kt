package dev.sabti.sidegloss.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TokenTest {
    @Test
    fun offsetsSelectTheTokenText() {
        val paragraph = "The keeper didn't sleep."
        val token = Token(text = "didn't", start = 11, end = 17, kind = TokenKind.WORD)

        assertEquals(token.text, paragraph.substring(token.start, token.end))
    }

    @Test
    fun rejectsStartAfterEnd() {
        assertFailsWith<IllegalArgumentException> {
            Token(text = "", start = 5, end = 4, kind = TokenKind.WORD)
        }
    }

    @Test
    fun rejectsNegativeStart() {
        assertFailsWith<IllegalArgumentException> {
            Token(text = "a", start = -1, end = 0, kind = TokenKind.WORD)
        }
    }

    @Test
    fun rejectsTextLengthThatDiffersFromOffsets() {
        assertFailsWith<IllegalArgumentException> {
            Token(text = "keeper", start = 4, end = 8, kind = TokenKind.WORD)
        }
    }
}
