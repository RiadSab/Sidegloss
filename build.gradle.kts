plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.spotless)
}

val ktlintVersion = libs.versions.ktlint.get()

spotless {
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(ktlintVersion)
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            targetExclude("**/build/**")
            ktlint(ktlintVersion)
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(ktlintVersion)
        }
    }
}

/**
 * Prints how many lines of Kotlin are common to every client platform and how many are specific to one.
 * The contest scores the share of code that platforms have in common.
 */
abstract class SharedCodeReport : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:Internal
    abstract val root: DirectoryProperty

    @TaskAction
    fun report() {
        val rootDir = root.get().asFile
        val linesBySourceSet =
            sources.files
                .filter { it.extension == "kt" }
                .groupBy { file ->
                    val parts = file.relativeTo(rootDir).invariantSeparatorsPath.split("/")
                    "${parts[0]}/${parts[2]}"
                }.mapValues { (_, files) ->
                    files.sumOf { file -> file.readLines().count { it.isNotBlank() } }
                }.toSortedMap()

        linesBySourceSet.forEach { (sourceSet, lines) -> println("${sourceSet.padEnd(32)} $lines") }

        fun linesWhere(matches: (String) -> Boolean) = linesBySourceSet.filterKeys(matches).values.sum()

        val isTest = { sourceSet: String -> sourceSet.endsWith("Test") || sourceSet.endsWith("/test") }
        val isServer = { sourceSet: String -> sourceSet.startsWith("server/") }
        val common = linesWhere { it.endsWith("/commonMain") }
        val client = linesWhere { !isTest(it) && !isServer(it) }
        val server = linesWhere { !isTest(it) && isServer(it) }
        val tests = linesWhere(isTest)

        println()
        println("Client code common to all platforms: $common of $client lines, ${percent(common, client)}%")
        println("Client code specific to one platform: ${client - common} lines")
        println("Server code, which also runs the common engine: $server lines")
        println("Test code: $tests lines")
    }

    private fun percent(
        part: Int,
        whole: Int,
    ) = if (whole == 0) 0 else part * 100 / whole
}

tasks.register<SharedCodeReport>("sharedCodeReport") {
    group = "verification"
    description = "Reports the share of Kotlin client code that is common to all platforms."
    root = layout.projectDirectory
    sources.from(
        fileTree(layout.projectDirectory) {
            include("*/src/**/*.kt")
            exclude("**/build/**")
        },
    )
}
