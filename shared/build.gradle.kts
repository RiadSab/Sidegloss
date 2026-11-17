@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
}

val importEnglishWordForms by tasks.registering(ImportEnglishWordForms::class) {
    group = "data"
    description = "Rebuilds data/english-word-forms.txt from AGID's infl.txt, given with -Pagid=/path/to/infl.txt."
    agidFile = layout.file(providers.gradleProperty("agid").map { File(it) })
    output = layout.projectDirectory.file("data/english-word-forms.txt")
}

val generateEnglishWordForms by tasks.registering(GenerateEnglishWordForms::class) {
    description = "Compiles data/english-word-forms.txt into Kotlin constants for common code."
    data = layout.projectDirectory.file("data/english-word-forms.txt")
    outputDirectory = layout.buildDirectory.dir("generated/wordForms/commonMain/kotlin")
    mustRunAfter(importEnglishWordForms)
}

kotlin {
    android {
        namespace = "dev.sabti.sidegloss.shared"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    jvm()

    iosArm64()
    iosSimulatorArm64()

    wasmJs {
        browser {
            testTask { enabled = false }
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generateEnglishWordForms)
        }
        commonMain.dependencies {
            api(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

/**
 * Converts AGID, the Automatically Generated Inflection Database by Kevin Atkinson, into the word-form file that
 * ships with Sidegloss. AGID marks every form it is unsure of, and those are dropped: entries whose part of speech
 * is unknown, base words with capitals or of one letter, forms with a doubt marker (`~`, `<`, `!`, `?`) and forms
 * of variant level 2 or more. A form that extends its base is written `+` and the ending, which halves the file.
 */
abstract class ImportEnglishWordForms : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val agidFile: RegularFileProperty

    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    fun import() {
        val entryPattern = Regex("""(\S+) ([VNA])(\??): (.*)""")
        val formPattern = Regex("""([A-Za-z']+)([~<!?]*)(?: ([0-9.]+))?(?: \{[^}]*\})?""")
        val basePattern = Regex("[a-z]{2,}")
        val wordPattern = Regex("[a-z']+")
        val lines =
            agidFile.get().asFile.readLines(Charsets.ISO_8859_1).mapNotNull { line ->
                val (base, partOfSpeech, unsure, forms) =
                    checkNotNull(entryPattern.matchEntire(line)) { "Unexpected AGID line: $line" }.destructured
                if (unsure.isNotEmpty() || !basePattern.matches(base)) return@mapNotNull null
                val keptForms =
                    forms
                        .split(" | ")
                        .flatMap { it.split(", ") }
                        .mapNotNull { text ->
                            val (form, doubt, level) =
                                checkNotNull(
                                    formPattern.matchEntire(text),
                                ) { "Unexpected AGID form: $text" }.destructured
                            val isSure = doubt.isEmpty() && (level.toDoubleOrNull() ?: 0.0) < 2
                            form.takeIf { isSure && it != base && wordPattern.matches(it) }
                        }.distinct()
                        .map { if (it.startsWith(base)) "+${it.removePrefix(base)}" else it }
                (listOf(partOfSpeech, base) + keptForms).joinToString(" ")
            }
        output.get().asFile.writeText(HEADER + lines.joinToString("\n", postfix = "\n"))
    }

    private companion object {
        val HEADER =
            """
            # English word forms for Sidegloss, derived from AGID 2016.01.19 by Kevin Atkinson,
            # http://wordlist.aspell.net. AGID's copyright notice and the notices of its sources are in
            # AGID-README.txt, next to this file. Rebuild this file with
            # ./gradlew :shared:importEnglishWordForms -Pagid=/path/to/agid-2016.01.19/infl.txt
            #
            # Each line holds a part of speech (V verb, N noun, A adjective or adverb), a base word and its forms.
            # A form written +x is the base word followed by x. Only forms AGID is sure of are kept.
            """.trimIndent() + "\n"
    }
}

/**
 * Turns the word-form file into Kotlin string constants. Common code has no resource files on every platform, and
 * a constant is the one form of data that every Kotlin target compiles the same way. Each constant stays well under
 * the JVM limit of 65,535 bytes.
 */
abstract class GenerateEnglishWordForms : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val data: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val linePattern = Regex("[VNA]( [a-z'+]+)+")
        val lines =
            data
                .get()
                .asFile
                .readLines()
                .filter { it.isNotBlank() && !it.startsWith("#") }
        lines.forEach { check(linePattern.matches(it)) { "Unexpected word-form line: $it" } }

        val chunks = mutableListOf(mutableListOf<String>())
        var chunkLength = 0
        for (line in lines) {
            if (chunkLength + line.length > MAX_CHUNK_LENGTH) {
                chunks += mutableListOf<String>()
                chunkLength = 0
            }
            chunks.last() += line
            chunkLength += line.length + 1
        }

        val source =
            buildString {
                appendLine("// Generated from shared/data/english-word-forms.txt by :shared:generateEnglishWordForms.")
                appendLine("package dev.sabti.sidegloss.text")
                appendLine()
                appendLine("/** The lines of the English word-form file, in parts small enough for every target. */")
                appendLine("internal val ENGLISH_WORD_FORM_DATA: List<String> =")
                appendLine("    listOf(")
                chunks.indices.forEach { appendLine("        WORD_FORMS_$it,") }
                appendLine("    )")
                chunks.forEachIndexed { index, chunk ->
                    appendLine()
                    appendLine("private const val WORD_FORMS_$index = \"${chunk.joinToString("\\n")}\"")
                }
            }
        val file = outputDirectory.get().file("text/EnglishWordFormData.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(source)
    }

    private companion object {
        const val MAX_CHUNK_LENGTH = 60_000
    }
}
