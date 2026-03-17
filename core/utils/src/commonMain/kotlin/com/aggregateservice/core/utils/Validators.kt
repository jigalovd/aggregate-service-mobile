package com.aggregateservice.core.utils

object Validators {
    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    fun isValidPhone(phone: String): Boolean {
        val phoneRegex = Regex("^\\+?[1-9]\\d{6,14}$")
        return phoneRegex.matches(phone.replace(" ", "").replace("-", ""))
    }

    fun isNotEmpty(value: String): Boolean = value.isNotBlank()
}
