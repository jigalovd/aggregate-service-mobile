package com.aggregateservice.core.utils

import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import com.aggregateservice.core.config.Environment
import com.aggregateservice.core.config.Language
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PasswordValidatorTest {

    @BeforeTest
    fun setup() {
        Config.initialize(
            AppConfig(
                apiBaseUrl = "https://api.test.com",
                apiKey = "test-api-key",
                environmentCode = Environment.DEV.name,
                languageCode = Language.RU.code,
                isDebug = true,
                enableLogging = false,
                networkTimeoutMs = 30_000L,
                apiVersion = "v1",
                passwordMinLength = 12,
                passwordMaxLength = 128,
            )
        )
    }

    @AfterTest
    fun tearDown() {
        Config.reset()
    }

    @Test
    fun `validate returns Invalid when password is blank`() {
        val result = PasswordValidator.validate("")
        assertIs<ValidationResult.Invalid>(result)
        assertEquals("Пароль не может быть пустым", result.errorMessage)
    }

    @Test
    fun `validate returns Invalid when password contains only spaces`() {
        val result = PasswordValidator.validate("   ")
        assertIs<ValidationResult.Invalid>(result)
        assertEquals("Пароль не может быть пустым", result.errorMessage)
    }

    @Test
    fun `validate returns Invalid when password is too short`() {
        val result = PasswordValidator.validate("short")
        assertIs<ValidationResult.Invalid>(result)
        assertTrue(result.errorMessage.contains("12"))
    }

    @Test
    fun `validate returns Invalid when password is exactly min length minus one`() {
        val result = PasswordValidator.validate("a".repeat(11))
        assertIs<ValidationResult.Invalid>(result)
        assertTrue(result.errorMessage.contains("12"))
    }

    @Test
    fun `validate returns Valid when password is exactly min length`() {
        val result = PasswordValidator.validate("a".repeat(12))
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `validate returns Invalid when password exceeds max length`() {
        val result = PasswordValidator.validate("a".repeat(129))
        assertIs<ValidationResult.Invalid>(result)
        assertTrue(result.errorMessage.contains("128"))
    }

    @Test
    fun `validate returns Valid when password is exactly max length`() {
        val result = PasswordValidator.validate("a".repeat(128))
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `validate returns Invalid when password contains spaces`() {
        val result = PasswordValidator.validate("valid password123")
        assertIs<ValidationResult.Invalid>(result)
        assertEquals("Пароль не должен содержать пробелы", result.errorMessage)
    }

    @Test
    fun `validate returns Invalid when password has no letters`() {
        val result = PasswordValidator.validate("123456789012")
        assertIs<ValidationResult.Invalid>(result)
        assertEquals("Пароль должен содержать хотя бы одну букву", result.errorMessage)
    }

    @Test
    fun `validate returns Valid for password with only letters`() {
        val result = PasswordValidator.validate("validpassword")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `validate returns Valid for password with letters and numbers`() {
        val result = PasswordValidator.validate("ValidPassword123")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `validate returns Valid for password with special characters`() {
        val result = PasswordValidator.validate("ValidPass!@#123")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `isValidPassword extension returns true for valid password`() {
        assertTrue("ValidPassword123".isValidPassword())
    }

    @Test
    fun `isValidPassword extension returns false for invalid password`() {
        assertTrue(!"short".isValidPassword())
    }

    @Test
    fun `calculateStrength returns 0 for empty password`() {
        val strength = PasswordValidator.calculateStrength("")
        assertEquals(0, strength)
    }

    @Test
    fun `calculateStrength caps at 100`() {
        val strength = PasswordValidator.calculateStrength("VeryLongPasswordWith123And!@#")
        assertTrue(strength <= 100)
    }

    @Test
    fun `calculateStrength increases with length`() {
        val shortStrength = PasswordValidator.calculateStrength("abc")
        val longStrength = PasswordValidator.calculateStrength("abcdefghij")
        assertTrue(longStrength > shortStrength)
    }

    @Test
    fun `calculateStrength increases with digits`() {
        val noDigits = PasswordValidator.calculateStrength("abcdefghij")
        val withDigits = PasswordValidator.calculateStrength("abcdefghij1")
        assertTrue(withDigits > noDigits)
    }

    @Test
    fun `calculateStrength increases with uppercase`() {
        val noUpper = PasswordValidator.calculateStrength("abcdefghij")
        val withUpper = PasswordValidator.calculateStrength("Abcdefghij")
        assertTrue(withUpper > noUpper)
    }

    @Test
    fun `calculateStrength increases with special chars`() {
        val noSpecial = PasswordValidator.calculateStrength("abcdefghij")
        val withSpecial = PasswordValidator.calculateStrength("abcdefghij!")
        assertTrue(withSpecial > noSpecial)
    }
}
