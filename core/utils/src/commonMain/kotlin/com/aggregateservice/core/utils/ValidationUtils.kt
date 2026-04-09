package com.aggregateservice.core.utils

import com.aggregateservice.core.config.Config
import com.aggregateservice.core.utils.ValidationResult.Invalid
import com.aggregateservice.core.utils.ValidationResult.Valid

/**
 * Результат валидации поля.
 */
sealed class ValidationResult {
    /** Валидация прошла успешно */
    data object Valid : ValidationResult()

    /** Валидация не прошла */
    data class Invalid(
        val errorMessage: String,
    ) : ValidationResult()
}

/**
 * Валидатор для email адреса.
 */
object EmailValidator {
    /**
     * Проверяет формат email адреса.
     *
     * **Правила валидации:**
     * - Не должен быть пустым
     * - Должен содержать символ @
     * - Должен содержать домен (точку после @)
     * - Минимум 3 символа до @
     * - Минимум 2 символа после последней точки
     *
     * @param email Email адрес для проверки
     * @return [Valid] если email валиден, [Invalid] с сообщением об ошибке в противном случае
     */
    fun validate(email: String): ValidationResult {
        val trimmedEmail = email.trim()

        when {
            trimmedEmail.isBlank() -> {
                return Invalid("Email не может быть пустым")
            }

            !trimmedEmail.contains("@") -> {
                return Invalid("Email должен содержать символ @")
            }

            !trimmedEmail.contains(".") -> {
                return Invalid("Email должен содержать домен (точку)")
            }

            trimmedEmail.length < 6 -> {
                return Invalid("Email слишком короткий")
            }

            !isValidEmailFormat(trimmedEmail) -> {
                return Invalid("Неверный формат email")
            }

            else -> return Valid
        }
    }

    /**
     * Проверяет email с помощью regex.
     */
    private fun isValidEmailFormat(email: String): Boolean {
        val emailRegex =
            Regex(
                pattern = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""",
                options = setOf(RegexOption.IGNORE_CASE),
            )
        return emailRegex.matches(email)
    }
}

/**
 * Валидатор для пароля.
 *
 * **Important:** Использует Config для получения требований к длине пароля.
 */
object PasswordValidator {
    /**
     * Проверяет силу пароля.
     *
     * **Правила валидации:**
     * - Минимальная длина из Config.passwordMinLength (по умолчанию 12)
     * - Максимальная длина из Config.passwordMaxLength (по умолчанию 128)
     * - Должен содержать хотя бы одну букву
     * - Не должен содержать пробелы
     *
     * **Config:**
     * - Длина пароля настраивается через AppConfig.passwordMinLength
     * - Использует Config.passwordMinLength и Config.passwordMaxLength
     *
     * @param password Пароль для проверки
     * @return [Valid] если пароль валиден, [Invalid] с сообщением об ошибке в противном случае
     */
    fun validate(password: String): ValidationResult {
        val minLength = Config.passwordMinLength
        val maxLength = Config.passwordMaxLength

        when {
            password.isBlank() -> {
                return Invalid("Пароль не может быть пустым")
            }

            password.length < minLength -> {
                return Invalid("Пароль должен быть не менее $minLength символов")
            }

            password.length > maxLength -> {
                return Invalid("Пароль должен быть не более $maxLength символов")
            }

            password.contains(" ") -> {
                return Invalid("Пароль не должен содержать пробелы")
            }

            !password.any { it.isLetter() } -> {
                return Invalid("Пароль должен содержать хотя бы одну букву")
            }

            else -> return Valid
        }
    }

    /**
     * Проверяет силу пароля для индикатора сложности (опционально).
     *
     * @param password Пароль для проверки
     * @return 0-100 оценка силы пароля
     */
    fun calculateStrength(password: String): Int {
        var strength = 0

        // Длина (макс 40 баллов)
        strength += minOf(password.length * 2, 40)

        // Наличие цифр (20 баллов)
        if (password.any { it.isDigit() }) strength += 20

        // Наличие заглавных букв (20 баллов)
        if (password.any { it.isUpperCase() }) strength += 20

        // Наличие специальных символов (20 баллов)
        if (password.any { !it.isLetterOrDigit() }) strength += 20

        return minOf(strength, 100)
    }
}

/**
 * Extension функция для проверки email.
 */
fun String.isValidEmail(): Boolean = EmailValidator.validate(this) is Valid

/**
 * Extension функция для проверки пароля.
 */
fun String.isValidPassword(): Boolean = PasswordValidator.validate(this) is Valid
