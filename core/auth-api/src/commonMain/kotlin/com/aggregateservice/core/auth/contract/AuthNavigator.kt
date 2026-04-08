package com.aggregateservice.core.auth.contract

import cafe.adriel.voyager.core.screen.Screen

interface AuthNavigator {
    fun createLoginScreen(): Screen
    fun createRegisterScreen(): Screen?
}
