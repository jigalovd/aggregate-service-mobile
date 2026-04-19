package com.aggregateservice.core.i18n

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for FlattenI18n helper functions and LocalizedString data class.
 */
class FlattenI18nTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ========== FlattenI18n.extractString() tests ==========

    // Test 1: Basic extraction from JsonObject
    @Test
    fun `extractString returns correct value for matching locale`() {
        val jsonObj = buildJsonObject {
            put("_i18n", buildJsonObject {
                put("en", JsonPrimitive("Hello"))
                put("ru", JsonPrimitive("Привет"))
                put("he", JsonPrimitive("שלום"))
            })
        }

        assertEquals("Hello", FlattenI18n.extractString(jsonObj, AppLocale.EN))
        assertEquals("Привет", FlattenI18n.extractString(jsonObj, AppLocale.RU))
        assertEquals("שלום", FlattenI18n.extractString(jsonObj, AppLocale.HE))
    }

    @Test
    fun `extractString returns null when _i18n is missing`() {
        val jsonObj = buildJsonObject {
            put("name", JsonPrimitive("Simple"))
        }

        assertNull(FlattenI18n.extractString(jsonObj, AppLocale.EN))
    }

    @Test
    fun `extractString returns null when locale is missing from i18n`() {
        val jsonObj = buildJsonObject {
            put("_i18n", buildJsonObject {
                put("en", JsonPrimitive("Only English"))
            })
        }

        assertEquals("Only English", FlattenI18n.extractString(jsonObj, AppLocale.EN))
        assertNull(FlattenI18n.extractString(jsonObj, AppLocale.RU))
        assertNull(FlattenI18n.extractString(jsonObj, AppLocale.HE))
    }

    // Test 2: Extraction with fallback
    @Test
    fun `extractString with fallback returns primary when available`() {
        val jsonObj = buildJsonObject {
            put("_i18n", buildJsonObject {
                put("en", JsonPrimitive("English"))
                put("ru", JsonPrimitive("Русский"))
            })
        }

        assertEquals("English", FlattenI18n.extractString(jsonObj, AppLocale.EN, AppLocale.RU))
    }

    @Test
    fun `extractString with fallback returns fallback when primary unavailable`() {
        val jsonObj = buildJsonObject {
            put("_i18n", buildJsonObject {
                put("en", JsonPrimitive("English"))
            })
        }

        assertEquals("English", FlattenI18n.extractString(jsonObj, AppLocale.RU, AppLocale.EN))
        assertEquals("English", FlattenI18n.extractString(jsonObj, AppLocale.HE, AppLocale.EN))
    }

    @Test
    fun `extractString with fallback returns null when both unavailable`() {
        val jsonObj = buildJsonObject {
            put("_i18n", buildJsonObject {
                put("fr", JsonPrimitive("French"))
            })
        }

        assertNull(FlattenI18n.extractString(jsonObj, AppLocale.RU, AppLocale.DEFAULT))
    }

    // Test 3: Map extraction
    @Test
    fun `extractStringFromMap returns correct value`() {
        val map = mapOf(
            "_i18n" to mapOf(
                "en" to "Hello",
                "ru" to "Привет",
            ),
        )

        assertEquals("Hello", FlattenI18n.extractStringFromMap(map, AppLocale.EN))
        assertEquals("Привет", FlattenI18n.extractStringFromMap(map, AppLocale.RU))
    }

    @Test
    fun `extractStringFromMap returns null for missing locale`() {
        val map = mapOf(
            "_i18n" to mapOf(
                "en" to "English only",
            ),
        )

        assertEquals("English only", FlattenI18n.extractStringFromMap(map, AppLocale.EN))
        assertNull(FlattenI18n.extractStringFromMap(map, AppLocale.HE))
    }

    @Test
    fun `extractStringFromMap returns null when _i18n missing`() {
        val map = mapOf(
            "name" to "Simple",
            "description" to "No i18n",
        )

        assertNull(FlattenI18n.extractStringFromMap(map, AppLocale.EN))
    }

    @Test
    fun `extractStringFromMap with fallback works correctly`() {
        val map = mapOf(
            "_i18n" to mapOf(
                "en" to "English",
            ),
        )

        assertEquals("English", FlattenI18n.extractStringFromMap(map, AppLocale.RU, AppLocale.EN))
    }

    // ========== LocalizedString data class tests ==========

    // Test 4: get() method
    @Test
    fun `LocalizedString get returns correct locale value`() {
        val localized = LocalizedString(en = "Hello", ru = "Привет", he = "שלום")

        assertEquals("Hello", localized.get(AppLocale.EN))
        assertEquals("Привет", localized.get(AppLocale.RU))
        assertEquals("שלום", localized.get(AppLocale.HE))
    }

    @Test
    fun `LocalizedString get returns null for missing locale value`() {
        val localized = LocalizedString(en = "English only")

        assertEquals("English only", localized.get(AppLocale.EN))
        assertNull(localized.get(AppLocale.RU))
        assertNull(localized.get(AppLocale.HE))
    }

    // Test 5: get() with fallback
    @Test
    fun `LocalizedString get with fallback returns primary when available`() {
        val localized = LocalizedString(en = "English", ru = "Русский")

        assertEquals("English", localized.get(AppLocale.EN, AppLocale.RU))
        assertEquals("Русский", localized.get(AppLocale.RU, AppLocale.EN))
    }

    @Test
    fun `LocalizedString get with fallback returns fallback when primary unavailable`() {
        val localized = LocalizedString(en = "English")

        assertEquals("English", localized.get(AppLocale.RU, AppLocale.EN))
        assertEquals("English", localized.get(AppLocale.HE, AppLocale.EN))
    }

    @Test
    fun `LocalizedString get with fallback returns null when both unavailable`() {
        // Create a LocalizedString with no values at all
        val empty = LocalizedString()
        assertNull(empty.get(AppLocale.EN, AppLocale.RU))
        assertNull(empty.get(AppLocale.RU, AppLocale.HE))
    }

    // Test 6: getFirstAvailable()
    @Test
    fun `getFirstAvailable returns first non-null value`() {
        val full = LocalizedString(en = "English", ru = "Русский", he = "שלום")
        assertEquals("English", full.getFirstAvailable())

        val noEn = LocalizedString(ru = "Русский", he = "שלום")
        assertEquals("Русский", noEn.getFirstAvailable())

        val onlyHe = LocalizedString(he = "שלום")
        assertEquals("שלום", onlyHe.getFirstAvailable())
    }

    @Test
    fun `getFirstAvailable returns null when all null`() {
        val empty = LocalizedString()
        assertNull(empty.getFirstAvailable())
    }

    // Test 7: fromI18nMap factory
    @Test
    fun `fromI18nMap creates correct LocalizedString`() {
        val map = mapOf(
            "_i18n" to mapOf(
                "en" to "English",
                "ru" to "Русский",
                "he" to "שלום",
            ),
        )

        val localized = LocalizedString.fromI18nMap(map)

        assertEquals("English", localized.en)
        assertEquals("Русский", localized.ru)
        assertEquals("שלום", localized.he)
    }

    @Test
    fun `fromI18nMap handles partial translations`() {
        val map = mapOf(
            "_i18n" to mapOf(
                "en" to "English only",
            ),
        )

        val localized = LocalizedString.fromI18nMap(map)

        assertEquals("English only", localized.en)
        assertNull(localized.ru)
        assertNull(localized.he)
    }

    @Test
    fun `fromI18nMap returns empty LocalizedString when _i18n missing`() {
        val map = mapOf(
            "name" to "Simple",
        )

        val localized = LocalizedString.fromI18nMap(map)

        assertNull(localized.en)
        assertNull(localized.ru)
        assertNull(localized.he)
    }

    // Test 8: fromString factory
    @Test
    fun `fromString fills all locales with same value`() {
        val localized = LocalizedString.fromString("Common text")

        assertEquals("Common text", localized.en)
        assertEquals("Common text", localized.ru)
        assertEquals("Common text", localized.he)
    }

    // Test 9: Empty values handling
    @Test
    fun `LocalizedString handles empty strings`() {
        val localized = LocalizedString(en = "", ru = null, he = "שלום")

        assertEquals("", localized.get(AppLocale.EN))
        assertNull(localized.get(AppLocale.RU))
        assertEquals("שלום", localized.get(AppLocale.HE))
    }

    @Test
    fun `fromI18nMap handles empty string values`() {
        val map = mapOf(
            "_i18n" to mapOf(
                "en" to "",
                "ru" to "Русский",
            ),
        )

        val localized = LocalizedString.fromI18nMap(map)

        assertEquals("", localized.en)
        assertEquals("Русский", localized.ru)
    }

    // Test 10: Extension functions
    @Test
    fun `JsonObject extractI18nString extension works`() {
        val jsonObj = buildJsonObject {
            put("_i18n", buildJsonObject {
                put("en", JsonPrimitive("Hello"))
                put("ru", JsonPrimitive("Привет"))
            })
        }

        assertEquals("Hello", jsonObj.extractI18nString(AppLocale.EN))
        assertEquals("Привет", jsonObj.extractI18nString(AppLocale.RU))
    }

    @Test
    fun `Map extractI18nString extension works`() {
        val map = mapOf(
            "_i18n" to mapOf(
                "en" to "Hello",
                "ru" to "Привет",
            ),
        )

        assertEquals("Hello", map.extractI18nString(AppLocale.EN))
        assertEquals("Привет", map.extractI18nString(AppLocale.RU))
    }

    // Test 11: Complex nested structures
    @Test
    fun `extractString handles deeply nested i18n objects`() {
        val jsonObj = buildJsonObject {
            put("title", buildJsonObject {
                put("_i18n", buildJsonObject {
                    put("en", JsonPrimitive("English Title"))
                })
            })
            put("description", JsonPrimitive("Plain text"))
        }

        val titleObj = jsonObj["title"]?.jsonObject
        assertTrue(titleObj != null)
        assertEquals("English Title", FlattenI18n.extractString(titleObj!!, AppLocale.EN))
    }

    // Test 12: fromI18nMap with type-safe map
    @Test
    fun `fromI18nMap handles null values in map`() {
        val map = mapOf(
            "_i18n" to mapOf(
                "en" to "English",
                "ru" to null,
                "he" to null,
            ),
        )

        val localized = LocalizedString.fromI18nMap(map)

        assertEquals("English", localized.en)
        assertNull(localized.ru)
        assertNull(localized.he)
    }

    // Test 13: getFirstAvailable priority
    @Test
    fun `getFirstAvailable follows en-ru-he priority`() {
        val onlyRu = LocalizedString(ru = "Русский")
        assertEquals("Русский", onlyRu.getFirstAvailable())

        val onlyHe = LocalizedString(he = "שלום")
        assertEquals("שלום", onlyHe.getFirstAvailable())
    }

    // Test 14: toString representation
    @Test
    fun `LocalizedString toString is meaningful`() {
        val localized = LocalizedString(en = "Hello", ru = "Привет")
        val str = localized.toString()
        assertTrue(str.contains("Hello"))
        assertTrue(str.contains("Привет"))
    }
}
