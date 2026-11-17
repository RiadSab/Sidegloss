package dev.sabti.sidegloss.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WordFormTableTest {
    private val table =
        WordFormTable(
            listOf(
                "# A comment line",
                "",
                "V hope +d hoping +s",
                "V hop +ped +ping +s",
                "N leaf leaves",
                "V leave left +d leaving +s",
                "N layer +s",
                "A lay +er",
                "A clean +er +est",
                "N cleaner +s",
                "N it +s",
            ),
        )

    @Test
    fun expandsFormsWrittenFromTheBase() {
        assertEquals(listOf("hope"), table.lemmasOf("hoped"))
        assertEquals(listOf("hope"), table.lemmasOf("hoping"))
        assertEquals(listOf("hop"), table.lemmasOf("hopping"))
    }

    @Test
    fun listsEveryBaseOfASharedForm() {
        assertEquals(listOf("leaf", "leave"), table.lemmasOf("leaves"))
    }

    @Test
    fun knowsBasesAndForms() {
        assertTrue("hope" in table)
        assertTrue("hoping" in table)
        assertTrue(table.isBase("layer"))
        assertFalse(table.isBase("hoping"))
        assertFalse("holmes" in table)
    }

    @Test
    fun skipsComparisonsThatAreWordsOfTheirOwn() {
        assertEquals(emptyList(), table.lemmasOf("layer"))
        assertEquals(listOf("clean"), table.lemmasOf("cleaner"))
        assertEquals(listOf("clean"), table.lemmasOf("cleanest"))
    }

    @Test
    fun skipsFunctionWords() {
        assertFalse(table.isBase("it"))
        assertEquals(emptyList(), table.lemmasOf("its"))
    }

    @Test
    fun rejectsAMalformedLine() {
        assertFailsWith<IllegalArgumentException> { WordFormTable(listOf("hope")) }
    }

    @Test
    fun loadsTheDataCompiledIntoTheApp() {
        val english = WordFormTable.english

        assertTrue("aardvark" in english)
        assertTrue("zoom" in english)
        assertEquals(listOf("run"), english.lemmasOf("ran"))
        assertEquals(listOf("hope"), english.lemmasOf("hoping"))
        assertEquals(listOf("half", "halve"), english.lemmasOf("halves"))
    }
}
