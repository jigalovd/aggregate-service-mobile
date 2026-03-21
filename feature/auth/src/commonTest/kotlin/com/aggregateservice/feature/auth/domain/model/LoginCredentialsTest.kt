package com.aggregateservice.feature.auth.domain.model

import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import com.aggregateservice.core.config.Environment
import com.aggregateservice.core.config.Language
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LoginCredentialsTest {

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
    fun `should create credentials with valid email and password`() {
        val email = "test@example.com"
        val password = "ValidPassword123"

        val credentials = LoginCredentials(email = email, password = password)

        assertEquals(email, credentials.email)
        assertEquals(password, credentials.password)
    }

    @Test
    fun `should throw exception when email is blank`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            LoginCredentials(email = "", password = "ValidPassword123")
        }
        assertTrue(exception.message?.contains("Email cannot be blank") == true)
    }

    @Test
    fun `should throw exception when email contains only whitespace`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            LoginCredentials(email = "   ", password = "ValidPassword123")
        }
        assertTrue(exception.message?.contains("Email cannot be blank") == true)
    }

    @Test
    fun `should throw exception when password is blank`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            LoginCredentials(email = "test@example.com", password = "")
        }
        assertTrue(exception.message?.contains("Password cannot be blank") == true)
    }

    @Test
    fun `should throw exception when password contains only whitespace`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            LoginCredentials(email = "test@example.com", password = "      ")
        }
        assertTrue(exception.message?.contains("Password cannot be blank") == true)
    }

    @Test
    fun `should throw exception when password is too short`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            LoginCredentials(email = "test@example.com", password = "Short123")
        }
        assertTrue(exception.message?.contains("Password must be at least 12 characters") == true)
    }

    @Test
    fun `should accept password with exactly minimum length`() {
        // Minimum is 12 characters
        val password = "123456789012"
        assertEquals(12, password.length)

        val credentials = LoginCredentials(email = "test@example.com", password = password)

        assertEquals(password, credentials.password)
    }

    @Test
    fun `should accept password longer than minimum`() {
        val password = "VeryLongPassword123!@#"
        assertTrue(password.length > 12)

        val credentials = LoginCredentials(email = "test@example.com", password = password)

        assertEquals(password, credentials.password)
    }

    @Test
    fun `should accept various email formats`() {
        val validEmails = listOf(
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.com",
            "user@subdomain.example.com",
            "user123@example.org",
            "USER@EXAMPLE.COM", // Uppercase
        )

        validEmails.forEach { email ->
            val credentials = LoginCredentials(email = email, password = "ValidPassword123")
            assertEquals(email, credentials.email)
        }
    }

    @Test
    fun `should preserve email case`() {
        val mixedCaseEmail = "Test.User@Example.Com"
        val credentials = LoginCredentials(email = mixedCaseEmail, password = "ValidPassword123")
        assertEquals(mixedCaseEmail, credentials.email)
    }

    @Test
    fun `should preserve password exactly as provided`() {
        val password = "Complex!@#$%Password123"
        val credentials = LoginCredentials(email = "test@example.com", password = password)
        assertEquals(password, credentials.password)
    }

    @Test
    fun `should be a data class with proper equals`() {
        val credentials1 = LoginCredentials(email = "test@example.com", password = "ValidPassword123")
        val credentials2 = LoginCredentials(email = "test@example.com", password = "ValidPassword123")

        assertEquals(credentials1, credentials2)
    }

    @Test
    fun `should have proper hashCode`() {
        val credentials1 = LoginCredentials(email = "test@example.com", password = "ValidPassword123")
        val credentials2 = LoginCredentials(email = "test@example.com", password = "ValidPassword123")

        assertEquals(credentials1.hashCode(), credentials2.hashCode())
    }

    @Test
    fun `should have proper toString`() {
        val credentials = LoginCredentials(email = "test@example.com", password = "ValidPassword123")

        val stringRepresentation = credentials.toString()

        assertTrue(stringRepresentation.contains("LoginCredentials"))
        assertTrue(stringRepresentation.contains("test@example.com"))
        assertTrue(stringRepresentation.contains("ValidPassword123"))
    }
}
