package com.aggregateservice.core.i18n

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests for AppLocale enum and its parsing functions.
 */
class AppLocaleTest {

    // Test 1: Enum entries exist
    @Test
    fun `RU locale has correct properties`() {
        val locale = AppLocale.RU
        assertEquals("ru", locale.code)
        assertEquals("Russian", locale.displayName)
        assertEquals("Русский", locale.nativeName)
        assertFalse(locale.isRtl)
    }

    @Test
    fun `HE locale has correct properties`() {
        val locale = AppLocale.HE
        assertEquals("he", locale.code)
        assertEquals("Hebrew", locale.displayName)
        assertEquals("עברית", locale.nativeName)
        assertTrue(locale.isRtl)
    }

    @Test
    fun `EN locale has correct properties`() {
        val locale = AppLocale.EN
        assertEquals("en", locale.code)
        assertEquals("English", locale.displayName)
        assertEquals("English", locale.nativeName)
        assertFalse(locale.isRtl)
    }

    // Test 2: DEFAULT is EN
    @Test
    fun `DEFAULT locale is English`() {
        assertEquals(AppLocale.EN, AppLocale.DEFAULT)
    }

    // Test 3: ALL contains all locales
    @Test
    fun `ALL contains all three locales`() {
        val all = AppLocale.ALL
        assertEquals(3, all.size)
        assertTrue(all.contains(AppLocale.RU))
        assertTrue(all.contains(AppLocale.HE))
        assertTrue(all.contains(AppLocale.EN))
    }

    // Test 4: fromCode() parsing
    @Test
    fun `fromCode parses lowercase code`() {
        assertEquals(AppLocale.RU, AppLocale.fromCode("ru"))
        assertEquals(AppLocale.HE, AppLocale.fromCode("he"))
        assertEquals(AppLocale.EN, AppLocale.fromCode("en"))
    }

    @Test
    fun `fromCode parses uppercase code`() {
        assertEquals(AppLocale.RU, AppLocale.fromCode("RU"))
        assertEquals(AppLocale.HE, AppLocale.fromCode("HE"))
        assertEquals(AppLocale.EN, AppLocale.fromCode("EN"))
    }

    @Test
    fun `fromCode parses mixed case code`() {
        assertEquals(AppLocale.RU, AppLocale.fromCode("Ru"))
        assertEquals(AppLocale.HE, AppLocale.fromCode("He"))
        assertEquals(AppLocale.EN, AppLocale.fromCode("En"))
    }

    @Test
    fun `fromCode returns DEFAULT for unknown code`() {
        assertEquals(AppLocale.DEFAULT, AppLocale.fromCode("fr"))
        assertEquals(AppLocale.DEFAULT, AppLocale.fromCode("de"))
        assertEquals(AppLocale.DEFAULT, AppLocale.fromCode("zh"))
        assertEquals(AppLocale.DEFAULT, AppLocale.fromCode(""))
        assertEquals(AppLocale.DEFAULT, AppLocale.fromCode("unknown"))
        assertEquals(AppLocale.DEFAULT, AppLocale.fromCode("xyz"))
    }

    // Test 5: fromTag() parsing with region
    @Test
    fun `fromTag parses language code from tag`() {
        assertEquals(AppLocale.RU, AppLocale.fromTag("ru-RU"))
        assertEquals(AppLocale.HE, AppLocale.fromTag("he-IL"))
        assertEquals(AppLocale.EN, AppLocale.fromTag("en-US"))
        assertEquals(AppLocale.EN, AppLocale.fromTag("en-GB"))
    }

    @Test
    fun `fromTag handles lowercase language code`() {
        assertEquals(AppLocale.RU, AppLocale.fromTag("ru-ru"))
        assertEquals(AppLocale.HE, AppLocale.fromTag("he-il"))
    }

    @Test
    fun `fromTag returns DEFAULT for unknown language`() {
        assertEquals(AppLocale.DEFAULT, AppLocale.fromTag("fr-FR"))
        assertEquals(AppLocale.DEFAULT, AppLocale.fromTag("de-DE"))
        assertEquals(AppLocale.DEFAULT, AppLocale.fromTag("ja-JP"))
        assertEquals(AppLocale.DEFAULT, AppLocale.fromTag("xyz-XY"))
    }

    @Test
    fun `fromTag handles bare language tag`() {
        assertEquals(AppLocale.RU, AppLocale.fromTag("ru"))
        assertEquals(AppLocale.HE, AppLocale.fromTag("he"))
        assertEquals(AppLocale.EN, AppLocale.fromTag("en"))
    }

    // Test 6: RTL detection
    @Test
    fun `only Hebrew is RTL`() {
        assertTrue(AppLocale.HE.isRtl)
        assertFalse(AppLocale.EN.isRtl)
        assertFalse(AppLocale.RU.isRtl)
    }

    // Test 7: Entries match individual locale objects
    @Test
    fun `entries match ALL`() {
        assertEquals(AppLocale.entries, AppLocale.ALL)
    }

    // Test 8: fromCode is case-insensitive
    @Test
    fun `fromCode is case insensitive`() {
        assertEquals(AppLocale.RU, AppLocale.fromCode("RU"))
        assertEquals(AppLocale.RU, AppLocale.fromCode("rU"))
        assertEquals(AppLocale.RU, AppLocale.fromCode("ru"))

        assertEquals(AppLocale.HE, AppLocale.fromCode("HE"))
        assertEquals(AppLocale.HE, AppLocale.fromCode("hE"))
        assertEquals(AppLocale.HE, AppLocale.fromCode("he"))
    }

    // Test 9: Code consistency across locales
    @Test
    fun `all codes are two characters`() {
        AppLocale.entries.forEach { locale ->
            assertEquals(2, locale.code.length)
        }
    }

    @Test
    fun `codes are lowercase`() {
        AppLocale.entries.forEach { locale ->
            assertEquals(locale.code, locale.code.lowercase())
        }
    }

    // Test 10: Native names are non-empty
    @Test
    fun `all native names are non-empty`() {
        AppLocale.entries.forEach { locale ->
            assertTrue(locale.nativeName.isNotEmpty())
        }
    }

    // Test 11: Display names are non-empty
    @Test
    fun `all display names are non-empty`() {
        AppLocale.entries.forEach { locale ->
            assertTrue(locale.displayName.isNotEmpty())
        }
    }

    // Test 12: Equality and hashCode
    @Test
    fun `same locale instances are equal`() {
        val a = AppLocale.fromCode("en")
        val b = AppLocale.fromCode("EN")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `different locales are not equal`() {
        assertNotEquals(AppLocale.EN, AppLocale.RU)
        assertNotEquals(AppLocale.RU, AppLocale.HE)
        assertNotEquals(AppLocale.HE, AppLocale.EN)
    }

    // Test 13: Edge cases for fromTag
    @Test
    fun `fromTag handles region with multiple dashes`() {
        assertEquals(AppLocale.EN, AppLocale.fromTag("en-US-LATN"))
        assertEquals(AppLocale.RU, AppLocale.fromTag("ru-RU-PEREYSK"))
    }

    @Test
    fun `fromTag handles underscore variant`() {
        // Just to ensure it doesn't crash on weird input
        assertEquals(AppLocale.DEFAULT, AppLocale.fromTag("en_US"))
    }

    // Test 14: Default value consistency
    @Test
    fun `DEFAULT fromCode matches fromTag`() {
        assertEquals(AppLocale.fromCode("xx"), AppLocale.fromTag("xx"))
        assertEquals(AppLocale.DEFAULT, AppLocale.fromCode("xx"))
    }
}
