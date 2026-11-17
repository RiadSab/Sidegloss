package dev.sabti.sidegloss.text

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Identifies a [Document].
 *
 * Wrapping the string keeps a text id from being passed where another kind of id is expected. In JSON it is a
 * plain string.
 */
@Serializable
@JvmInline
value class TextId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "A text id must not be blank" }
    }
}
