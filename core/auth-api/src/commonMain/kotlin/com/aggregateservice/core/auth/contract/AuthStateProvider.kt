package com.aggregateservice.core.auth.contract

import com.aggregateservice.core.auth.state.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthStateProvider {
    val authState: StateFlow<AuthState>
}
