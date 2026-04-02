package com.aggregateservice.core.navigation

import cafe.adriel.voyager.core.screen.Screen

/**
 * Интерфейс для навигации к auth экранам.
 *
 * Решает проблему циклической зависимости между feature:favorites/feature:profile и feature:auth.
 * Реализация предоставляется в :feature:auth через DI.
 */
interface AuthNavigator {
    /**
     * Создаёт экран логина с Google Sign-In.
     *
     * @return Voyager Screen для логина
     */
    fun createLoginScreen(): Screen

    /**
     * Создаёт экран регистрации.
     *
     * @return Voyager Screen для регистрации, или null если не реализовано
     */
    fun createRegisterScreen(): Screen?
}
