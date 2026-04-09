package com.aggregateservice.core.auth.impl.token

import com.aggregateservice.core.storage.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TokenManagerImpl(
    private val tokenStorage: TokenStorage,
) : TokenManager {
    private val mutex = Mutex()
    private val _token = MutableStateFlow<String?>(null)
    private val _tokenFlow = _token.asStateFlow()

    override suspend fun getAccessToken(): String? = mutex.withLock { _token.value }

    override suspend fun setTokens(accessToken: String) =
        mutex.withLock {
            _token.value = accessToken
            tokenStorage.saveAccessToken(accessToken)
        }

    override suspend fun clearTokens() =
        mutex.withLock {
            _token.value = null
            tokenStorage.clearTokens()
        }

    override fun observeToken(): StateFlow<String?> = _tokenFlow

    override suspend fun initFromStorage() =
        mutex.withLock {
            _token.value = tokenStorage.getAccessTokenSync()
        }
}
