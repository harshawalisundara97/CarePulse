package com.carepulse.app.resources

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

/**
 * Every user-facing string must exist in English, Sinhala and Tamil, with the same format
 * placeholders, so no locale silently falls back to English or crashes on String.format.
 */
class StringResourceParityTest {

    private val resDir = File("src/main/res")
    private val locales = listOf("values-si", "values-ta")
    private val placeholder = Regex("""%(\d+\$)?[-#+ 0,(]*\d*(\.\d+)?[sdfx]""")

    /** name -> text for every translatable <string> in [dir]/strings.xml. */
    private fun strings(dir: String): Map<String, String> {
        val file = File(resDir, "$dir/strings.xml")
        assertTrue("Missing ${file.path}", file.exists())
        val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(file).getElementsByTagName("string")
        return (0 until nodes.length)
            .map { nodes.item(it) as Element }
            .filter { it.getAttribute("translatable") != "false" }
            .associate { it.getAttribute("name") to it.textContent }
    }

    private fun placeholders(text: String): List<String> =
        placeholder.findAll(text).map { it.value }.sorted().toList()

    @Test
    fun `every locale defines exactly the default string keys`() {
        val base = strings("values").keys
        for (locale in locales) {
            val keys = strings(locale).keys
            assertEquals("Keys missing from $locale", emptySet<String>(), base - keys)
            assertEquals("Extra keys in $locale", emptySet<String>(), keys - base)
        }
    }

    @Test
    fun `translations keep the same format placeholders`() {
        val base = strings("values")
        for (locale in locales) {
            strings(locale).forEach { (name, text) ->
                val expected = base[name] ?: return@forEach
                assertEquals(
                    "Placeholder mismatch for '$name' in $locale",
                    placeholders(expected),
                    placeholders(text)
                )
            }
        }
    }
}
