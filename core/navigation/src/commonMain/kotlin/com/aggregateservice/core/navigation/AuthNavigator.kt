package com.aggregateservice.core.navigation

import cafe.adriel.voyager.core.screen.Screen

/**
 * Интерфейс для навигации к auth экранам.
 *
 * Решает проблему циклической зависимости между feature:favorites/feature:profile и feature:auth.
 * Реализация предоставляется в :feature:auth через DI.
 *
 * **Note:** Auth is now handled via [com.aggregateservice.feature.auth.presentation.component.AuthPromptDialog]
 * with Firebase Auth callbacks. These methods return null because no screen navigation is needed.
 */
interface AuthNavigator {
    /**
     * Создаёт экран логина.
     *
     * @return Voyager Screen для логина, или null если auth обрабатывается через AuthPromptDialog
     */
    fun createLoginScreen(): Screen?

    /**
     * Создаёт экран регистрации.
     *
     * @return Voyager Screen для регистрации, или null если auth обрабатывается через AuthPromptDialog
     */
    fun createRegisterScreen(): Screen?
}
