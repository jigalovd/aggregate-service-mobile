package com.aggregateservice.feature.auth

import cafe.adriel.voyager.core.screen.Screen
import com.aggregateservice.core.auth.contract.AuthNavigator
import com.aggregateservice.feature.auth.presentation.screen.LoginScreen

class AuthNavigatorImpl : AuthNavigator {
    override fun createLoginScreen(): Screen = LoginScreen()
    override fun createRegisterScreen(): Screen? = null
}
