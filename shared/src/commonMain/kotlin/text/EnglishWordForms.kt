package dev.sabti.sidegloss.text

/**
 * Forms that [WordFormTable] leaves out or orders differently from how they are usually read. Comparisons such as
 * `better` and `more` are irregular, so the table cannot tell them from words of their own. `does` is almost never
 * the plural of `doe`. A stranger is more often a person than a comparison, and to flatter is more common than to
 * be flatter.
 */
internal val ENGLISH_WORD_FORMS =
    mapOf(
        "does" to listOf("do"),
        "better" to listOf("good", "well"),
        "best" to listOf("good", "well"),
        "further" to listOf("far"),
        "least" to listOf("little"),
        "less" to listOf("little"),
        "more" to listOf("much", "many"),
        "most" to listOf("much", "many"),
        "worse" to listOf("bad", "badly"),
        "worst" to listOf("bad", "badly"),
        "flatter" to listOf("flatter", "flat"),
        "stranger" to listOf("stranger", "strange"),
    )

/**
 * Articles, pronouns, prepositions, particles and conjunctions. They never inflect, so when a suffix rule turns
 * `offer` into `off` or `its` into `it`, the rule has misfired and the candidate is dropped. A particle invented
 * this way would otherwise let `offer` complete a phrasal verb such as "turn … off".
 */
internal val ENGLISH_FUNCTION_WORDS =
    (
        "a an the and but or nor so yet if as than i me we us you he him she her it its they them my our your his " +
            "their this that these those at by for from in into of off on onto out to up upon with"
    ).split(' ').toSet()

/** Adjectives and adverbs that compare with -er and -est, the only bases those suffixes may lead to. */
internal val COMPARABLE_ENGLISH_ADJECTIVES =
    (
        "angry big black bland blue blunt bold brave brief bright broad brown busy calm cheap clean clear clever " +
            "close cloudy cold cool costly cosy cozy crazy crisp cruel cute damp dark deadly dear deep dense dim " +
            "dirty dry dull dumb dusty early easy empty faint fair fast fat few fierce filthy fine firm fit flat " +
            "fond foul free fresh friendly full funny gentle glad grand gray great green grey grim gross happy hard " +
            "harsh healthy heavy high holy hot huge humble hungry icy jolly keen kind large late lazy lean light " +
            "likely lively lonely long loose loud lovely low lucky mad mean merry mild moist muddy narrow near neat " +
            "new nice noble noisy numb odd old pale plain poor pretty proud pure quick quiet rainy rare raw red rich " +
            "ripe rosy rough rude sad safe sane scarce shallow sharp short shy sick silly simple slim slow sly small " +
            "smart smooth soft soon sore sour steady steep stern stiff stormy strange strict strong sunny sure sweet " +
            "swift tall tame tasty tender thick thin thirsty tidy tight tiny tough true ugly vague vast warm weak " +
            "wealthy weird wet white wide wild windy wise worthy yellow young"
    ).split(' ').toSet()

/** Words that end like an inflected form but are not one, such as `news` or `evening`. */
internal val UNINFLECTED_ENGLISH_WORDS =
    (
        "always news perhaps sometimes series species whereas physics mathematics politics nothing something " +
            "anything everything evening morning during hundred clothes yes pants wicked crooked rugged ragged " +
            "wretched jagged shilling pudding earring feed weed"
    ).split(' ').toSet()
