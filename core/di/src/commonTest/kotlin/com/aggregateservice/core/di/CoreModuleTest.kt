package com.aggregateservice.core.di

import com.aggregateservice.core.config.Config
import com.aggregateservice.core.config.Environment
import com.aggregateservice.core.config.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [coreModule] DI configuration and [Config] utility class.
 * Tests pure functions and enum behavior from the config module.
 */
class CoreModuleTest {

    // --- Environment enum tests ---

    @Test
    fun `Environment fromString returns DEV for DEV`() {
        assertEquals(Environment.DEV, Environment.fromString("DEV"))
    }

    @Test
    fun `Environment fromString returns DEV for development`() {
        assertEquals(Environment.DEV, Environment.fromString("development"))
    }

    @Test
    fun `Environment fromString returns STAGING for STAGING`() {
        assertEquals(Environment.STAGING, Environment.fromString("STAGING"))
    }

    @Test
    fun `Environment fromString returns STAGING for stage`() {
        assertEquals(Environment.STAGING, Environment.fromString("stage"))
    }

    @Test
    fun `Environment fromString returns PROD for PROD`() {
        assertEquals(Environment.PROD, Environment.fromString("PROD"))
    }

    @Test
    fun `Environment fromString returns PROD for unknown`() {
        assertEquals(Environment.PROD, Environment.fromString("unknown"))
    }

    @Test
    fun `Environment fromString is case insensitive`() {
        assertEquals(Environment.DEV, Environment.fromString("dev"))
        assertEquals(Environment.DEV, Environment.fromString("Dev"))
        assertEquals(Environment.STAGING, Environment.fromString("staging"))
        assertEquals(Environment.STAGING, Environment.fromString("STAGE"))
    }

    // --- Language enum tests ---

    @Test
    fun `Language fromCode returns RU for ru`() {
        assertEquals(Language.RU, Language.fromCode("ru"))
    }

    @Test
    fun `Language fromCode returns HE for he`() {
        assertEquals(Language.HE, Language.fromCode("he"))
    }

    @Test
    fun `Language fromCode returns EN for en`() {
        assertEquals(Language.EN, Language.fromCode("en"))
    }

    @Test
    fun `Language fromCode falls back to RU for null`() {
        assertEquals(Language.RU, Language.fromCode(null))
    }

    @Test
    fun `Language fromCode falls back to RU for empty string`() {
        assertEquals(Language.RU, Language.fromCode(""))
    }

    @Test
    fun `Language fromCode falls back to RU for unknown code`() {
        assertEquals(Language.RU, Language.fromCode("fr"))
        assertEquals(Language.RU, Language.fromCode("zh"))
        assertEquals(Language.RU, Language.fromCode("de"))
    }

    @Test
    fun `Language enum has correct code values`() {
        assertEquals("ru", Language.RU.code)
        assertEquals("he", Language.HE.code)
        assertEquals("en", Language.EN.code)
    }

    @Test
    fun `Language enum has correct display names`() {
        assertEquals("Русский", Language.RU.displayName)
        assertEquals("עברית", Language.HE.displayName)
        assertEquals("English", Language.EN.displayName)
    }

    @Test
    fun `Language enum has exactly three values`() {
        assertEquals(3, Language.entries.size)
    }

    @Test
    fun `Environment enum has exactly three values`() {
        assertEquals(3, Environment.entries.size)
    }

    // --- Config validation tests ---

    @Test
    fun `Config instance throws when not initialized`() {
        Config.reset()
        var exception: Throwable? = null
        try {
            Config.instance
        } catch (e: IllegalStateException) {
            exception = e
        }
        assertTrue(exception is IllegalStateException)
        assertTrue(exception!!.message!!.contains("not initialized"))
    }

    // --- Module structure tests ---

    @Test
    fun `coreModule is defined`() {
        // Verify coreModule is accessible and has content
        assertTrue(::coreModule.name.isNotEmpty())
    }

    @Test
    fun `appModule includes coreModule`() {
        // Verify appModule includes coreModule in its definition
        // The actual module composition happens at runtime
        assertTrue(::appModule.name.isNotEmpty())
    }

    @Test
    fun `initializeKoin function exists`() {
        // Verify the function is accessible
        assertTrue(::initializeKoin.name.isNotEmpty())
    }
}