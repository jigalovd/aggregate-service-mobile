package com.aggregateservice.core.auth.impl.token

import kotlinx.coroutines.flow.StateFlow

interface TokenManager {
    suspend fun getAccessToken(): String?
    suspend fun setTokens(accessToken: String)
    suspend fun clearTokens()
    fun observeToken(): StateFlow<String?>
    suspend fun initFromStorage()
}
