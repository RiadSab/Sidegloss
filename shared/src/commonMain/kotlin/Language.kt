package dev.sabti.sidegloss

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A language Sidegloss can read texts in or write explanations in.
 *
 * The [tag] is the primary language subtag of an IETF language tag, such as `en`. It is also the form a language
 * takes in JSON.
 */
@Serializable
enum class Language(
    val tag: String,
) {
    @SerialName("en")
    ENGLISH("en"),

    @SerialName("fr")
    FRENCH("fr"),
    ;

    companion object {
        /**
         * Returns the language matching [tag], ignoring case and any region such as `fr-MA`,
         * or `null` when Sidegloss does not support it.
         */
        fun fromTag(tag: String): Language? {
            val primarySubtag = tag.substringBefore('-').substringBefore('_').lowercase()
            return entries.firstOrNull { it.tag == primarySubtag }
        }
    }
}
