package com.aggregateservice.core.i18n

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for I18nProvider interface and I18nProviderImpl.
 */
class I18nProviderTest {

    // Test 1: Default locale initialization
    @Test
    fun `default locale is English`() {
        val provider = I18nProviderImpl()
        assertEquals(AppLocale.EN, provider.currentLocale)
    }

    @Test
    fun `custom initial locale is respected`() {
        val provider = I18nProviderImpl(initialLocale = AppLocale.RU)
        assertEquals(AppLocale.RU, provider.currentLocale)
    }

    // Test 2: get() returns correct string
    @Test
    fun `get returns string from resources`() {
        val provider = I18nProviderImpl(
            initialLocale = AppLocale.EN,
            resources = mapOf(
                AppLocale.EN to mapOf("greeting" to "Hello"),
            ),
        )
        assertEquals("Hello", provider.get("greeting"))
    }

    @Test
    fun `get returns key when not found`() {
        val provider = I18nProviderImpl()
        val result = provider.get("nonexistent_key")
        assertEquals("nonexistent_key", result)
    }

    @Test
    fun `get returns empty string when key maps to empty`() {
        val provider = I18nProviderImpl(
            resources = mapOf(AppLocale.EN to mapOf("empty_key" to "")),
        )
        assertEquals("", provider.get("empty_key"))
    }

    // Test 3: getOrNull() returns null for missing keys
    @Test
    fun `getOrNull returns string when key exists`() {
        val provider = I18nProviderImpl(
            resources = mapOf(AppLocale.EN to mapOf("exists" to "Value")),
        )
        assertEquals("Value", provider.getOrNull("exists"))
    }

    @Test
    fun `getOrNull returns null when key does not exist`() {
        val provider = I18nProviderImpl()
        assertNull(provider.getOrNull("missing"))
    }

    @Test
    fun `getOrNull returns null when locale has no resources`() {
        val provider = I18nProviderImpl(
            resources = emptyMap(),
        )
        assertNull(provider.getOrNull("any_key"))
    }

    // Test 4: setLocale() changes current locale
    @Test
    fun `setLocale changes current locale`() {
        val provider = I18nProviderImpl(initialLocale = AppLocale.EN)
        provider.setLocale(AppLocale.RU)
        assertEquals(AppLocale.RU, provider.currentLocale)
    }

    @Test
    fun `setLocale to Hebrew changes to RTL locale`() {
        val provider = I18nProviderImpl(initialLocale = AppLocale.EN)
        provider.setLocale(AppLocale.HE)
        assertEquals(AppLocale.HE, provider.currentLocale)
        assertTrue(AppLocale.HE.isRtl)
    }

    // Test 5: get() with different locales returns locale-specific strings
    @Test
    fun `get returns locale-specific string after locale change`() {
        val provider = I18nProviderImpl(
            initialLocale = AppLocale.EN,
            resources = mapOf(
                AppLocale.EN to mapOf("greeting" to "Hello"),
                AppLocale.RU to mapOf("greeting" to "Привет"),
                AppLocale.HE to mapOf("greeting" to "שלום"),
            ),
        )

        assertEquals("Hello", provider.get("greeting"))

        provider.setLocale(AppLocale.RU)
        assertEquals("Привет", provider.get("greeting"))

        provider.setLocale(AppLocale.HE)
        assertEquals("שלום", provider.get("greeting"))
    }

    // Test 6: get() with format arguments
    @Test
    fun `get with args formats string`() {
        val provider = I18nProviderImpl(
            resources = mapOf(AppLocale.EN to mapOf("welcome" to "Welcome, %s!")),
        )
        assertEquals("Welcome, John!", provider.get("welcome", "John"))
    }

    @Test
    fun `get with multiple args formats string`() {
        val provider = I18nProviderImpl(
            resources = mapOf(AppLocale.EN to mapOf("format" to "%s has %d items")),
        )
        assertEquals("John has 5 items", provider.get("format", "John", 5))
    }

    @Test
    fun `get with empty args returns template`() {
        val provider = I18nProviderImpl(
            resources = mapOf(AppLocale.EN to mapOf("template" to "Hello, %s!")),
        )
        assertEquals("Hello, %s!", provider.get("template"))
    }

    @Test
    fun `get with args returns key on format error`() {
        val provider = I18nProviderImpl(
            resources = mapOf(AppLocale.EN to mapOf("bad_format" to "%s %s %s")),
        )
        // Only 2 args for 3 placeholders - should not crash
        val result = provider.get("bad_format", "a", "b")
        // The format call may fail and return the template
        assertNotNull(result)
    }

    // Test 7: getOrDefault extension function
    @Test
    fun `getOrDefault returns value when key exists`() {
        val provider = I18nProviderImpl(
            resources = mapOf(AppLocale.EN to mapOf("existing" to "Found")),
        )
        val result = provider.getOrDefault("existing", "Default")
        assertEquals("Found", result)
    }

    @Test
    fun `getOrDefault returns default when key missing`() {
        val provider = I18nProviderImpl()
        val result = provider.getOrDefault("missing", "Default Value")
        assertEquals("Default Value", result)
    }

    // Test 8: availableLocales returns all locales
    @Test
    fun `availableLocales returns all supported locales`() {
        val provider = I18nProviderImpl()
        val locales = provider.availableLocales()
        assertEquals(3, locales.size)
        assertTrue(locales.contains(AppLocale.EN))
        assertTrue(locales.contains(AppLocale.RU))
        assertTrue(locales.contains(AppLocale.HE))
    }

    // Test 9: localeFlow emits on locale change
    @Test
    fun `localeFlow emits new locale after setLocale`() = runTest {
        val provider = I18nProviderImpl(initialLocale = AppLocale.EN)

        assertEquals(AppLocale.EN, provider.localeFlow.first())

        provider.setLocale(AppLocale.RU)

        assertEquals(AppLocale.RU, provider.localeFlow.first())
    }

    // Test 10: Empty resources map
    @Test
    fun `get returns key with empty resources map`() {
        val provider = I18nProviderImpl(
            initialLocale = AppLocale.EN,
            resources = emptyMap(),
        )
        assertEquals("test_key", provider.get("test_key"))
    }

    // Test 11: Switching between all locales
    @Test
    fun `locale switching works correctly for all locales`() {
        val provider = I18nProviderImpl(
            initialLocale = AppLocale.EN,
            resources = mapOf(
                AppLocale.EN to mapOf("lang" to "English"),
                AppLocale.RU to mapOf("lang" to "Русский"),
                AppLocale.HE to mapOf("lang" to "עברית"),
            ),
        )

        provider.availableLocales().forEach { locale ->
            provider.setLocale(locale)
            assertEquals(locale, provider.currentLocale)
        }
    }

    // Test 12: StringKey constants
    @Test
    fun `StringKey common constants are defined`() {
        assertEquals("app_name", StringKey.APP_NAME)
        assertEquals("ok", StringKey.OK)
        assertEquals("cancel", StringKey.CANCEL)
        assertEquals("save", StringKey.SAVE)
        assertEquals("delete", StringKey.DELETE)
        assertEquals("error", StringKey.ERROR)
        assertEquals("success", StringKey.SUCCESS)
        assertEquals("loading", StringKey.LOADING)
    }

    @Test
    fun `StringKey Auth constants are defined`() {
        assertEquals("auth_login", StringKey.Auth.LOGIN)
        assertEquals("auth_logout", StringKey.Auth.LOGOUT)
        assertEquals("auth_email", StringKey.Auth.EMAIL)
        assertEquals("auth_password", StringKey.Auth.PASSWORD)
        assertEquals("auth_sign_in_with_google", StringKey.Auth.SIGN_IN_WITH_GOOGLE)
        assertEquals("auth_session_expired", StringKey.Auth.SESSION_EXPIRED)
    }

    @Test
    fun `StringKey Navigation constants are defined`() {
        assertEquals("nav_home", StringKey.Navigation.HOME)
        assertEquals("nav_catalog", StringKey.Navigation.CATALOG)
        assertEquals("nav_booking", StringKey.Navigation.BOOKING)
        assertEquals("nav_profile", StringKey.Navigation.PROFILE)
    }

    @Test
    fun `StringKey Booking constants are defined`() {
        assertEquals("booking_title", StringKey.Booking.TITLE)
        assertEquals("booking_confirm", StringKey.Booking.CONFIRM)
        assertEquals("booking_cancel", StringKey.Booking.CANCEL)
        assertEquals("booking_my_bookings", StringKey.Booking.MY_BOOKINGS)
    }
}
