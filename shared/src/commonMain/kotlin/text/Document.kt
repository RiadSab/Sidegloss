package dev.sabti.sidegloss.text

import dev.sabti.sidegloss.Language
import kotlinx.serialization.Serializable

/**
 * A text the reader can open: its [title], the [language] it is written in, and its [paragraphs] in reading order.
 *
 * Tokens are not stored. They are computed from each paragraph, so every offset in a [Token] points into one of
 * these strings.
 */
@Serializable
data class Document(
    val id: TextId,
    val title: String,
    val language: Language,
    val paragraphs: List<String>,
)
