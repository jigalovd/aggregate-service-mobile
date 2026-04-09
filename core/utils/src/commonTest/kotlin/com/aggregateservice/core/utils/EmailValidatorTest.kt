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

class EmailValidatorTest {
    private lateinit var testConfig: AppConfig

    @BeforeTest
    fun setup() {
        testConfig =
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
        Config.initialize(testConfig)
    }

    @AfterTest
    fun tearDown() {
        Config.reset()
    }

    @Test
    fun `validate returns Invalid when email is blank`() {
        val result = EmailValidator.validate("")
        assertIs<ValidationResult.Invalid>(result)
        assertEquals("Email не может быть пустым", result.errorMessage)
    }

    @Test
    fun `validate returns Invalid when email contains only spaces`() {
        val result = EmailValidator.validate("   ")
        assertIs<ValidationResult.Invalid>(result)
        assertEquals("Email не может быть пустым", result.errorMessage)
    }

    @Test
    fun `validate returns Invalid when email has no at symbol`() {
        val result = EmailValidator.validate("testemail.com")
        assertIs<ValidationResult.Invalid>(result)
        assertEquals("Email должен содержать символ @", result.errorMessage)
    }

    @Test
    fun `validate returns Invalid when email has no dot`() {
        val result = EmailValidator.validate("test@emailcom")
        assertIs<ValidationResult.Invalid>(result)
        assertEquals("Email должен содержать домен (точку)", result.errorMessage)
    }

    @Test
    fun `validate returns Invalid when email is too short`() {
        val result = EmailValidator.validate("a@b.c")
        assertIs<ValidationResult.Invalid>(result)
        assertEquals("Email слишком короткий", result.errorMessage)
    }

    @Test
    fun `validate returns Invalid when email format is wrong`() {
        val result = EmailValidator.validate("invalid@email.c")
        assertIs<ValidationResult.Invalid>(result)
        assertEquals("Неверный формат email", result.errorMessage)
    }

    @Test
    fun `validate returns Valid for correct email`() {
        val result = EmailValidator.validate("test@example.com")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `validate returns Valid for email with subdomain`() {
        val result = EmailValidator.validate("user@mail.example.com")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `validate returns Valid for email with plus sign`() {
        val result = EmailValidator.validate("user+tag@example.com")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `validate returns Valid for email with numbers`() {
        val result = EmailValidator.validate("user123@example123.com")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `validate trims whitespace`() {
        val result = EmailValidator.validate("  test@example.com  ")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `validate is case insensitive`() {
        val result = EmailValidator.validate("TEST@EXAMPLE.COM")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `isValidEmail extension returns true for valid email`() {
        assertTrue("test@example.com".isValidEmail())
    }

    @Test
    fun `isValidEmail extension returns false for invalid email`() {
        assertTrue(!"invalid".isValidEmail())
    }
}
