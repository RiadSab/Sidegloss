package dev.sabti.sidegloss

/**
 * A language Sidegloss can read texts in or write explanations in.
 *
 * The [tag] is the primary language subtag of an IETF language tag, such as `en`.
 */
enum class Language(
    val tag: String,
) {
    ENGLISH("en"),
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
