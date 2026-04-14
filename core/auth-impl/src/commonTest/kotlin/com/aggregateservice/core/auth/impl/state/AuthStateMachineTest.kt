package com.aggregateservice.core.auth.impl.state

import com.aggregateservice.core.auth.contract.RefreshTokenUseCase
import com.aggregateservice.core.auth.impl.repository.AuthRepository
import com.aggregateservice.core.auth.impl.repository.dto.UserResponse
import com.aggregateservice.core.auth.state.AuthState
import com.aggregateservice.core.storage.TokenStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AuthStateMachineTest {
    private lateinit var tokenStore: TokenStore
    private lateinit var repository: AuthRepository
    private lateinit var refreshTokenUseCase: RefreshTokenUseCase
    private lateinit var stateMachine: AuthStateMachine

    @BeforeTest
    fun setup() {
        tokenStore = mockk(relaxed = true)
        repository = mockk(relaxed = true)
        refreshTokenUseCase = mockk(relaxed = true)
        stateMachine = AuthStateMachine(tokenStore, repository, refreshTokenUseCase)
    }

    @Test
    fun `initial state is Loading`() =
        runTest {
            assertIs<AuthState.Loading>(stateMachine.state.value)
        }

    @Test
    fun `initialize with no token emits Guest`() =
        runTest {
            coEvery { tokenStore.getAccessToken() } returns null
            stateMachine.initialize()
            assertIs<AuthState.Guest>(stateMachine.state.value)
        }

    @Test
    fun `initialize with valid token emits Authenticated`() =
        runTest {
            coEvery { tokenStore.getAccessToken() } returns "valid-token"
            coEvery { repository.getCurrentUser() } returns
                Result.success(
                    UserResponse(id = "user-1", email = "test@example.com"),
                )
            stateMachine.initialize()
            val state = stateMachine.state.value
            assertIs<AuthState.Authenticated>(state)
            assertEquals("user-1", state.userId)
        }

    @Test
    fun `initialize with expired token attempts refresh and retries`() =
        runTest {
            coEvery { tokenStore.getAccessToken() } returns "expired-token"
            coEvery { repository.getCurrentUser() } returns Result.failure(Exception("Unauthorized"))
            coEvery { refreshTokenUseCase() } returns Result.success("new-token")
            // Second call to getCurrentUser succeeds with new token
            coEvery {
                repository.getCurrentUser()
            } returns Result.failure(Exception("Unauthorized")) andThen
                Result.success(
                    UserResponse(id = "user-1", email = "refreshed@example.com"),
                )

            stateMachine.initialize()
            val state = stateMachine.state.value
            assertIs<AuthState.Authenticated>(state)
            assertEquals("user-1", state.userId)
            assertEquals("refreshed@example.com", state.email)
            coVerify { refreshTokenUseCase() }
        }

    @Test
    fun `initialize with expired token emits Guest when refresh also fails`() =
        runTest {
            coEvery { tokenStore.getAccessToken() } returns "expired-token"
            coEvery { repository.getCurrentUser() } returns Result.failure(Exception("Unauthorized"))
            coEvery { refreshTokenUseCase() } returns Result.failure(Exception("Refresh failed"))

            stateMachine.initialize()
            assertIs<AuthState.Guest>(stateMachine.state.value)
        }

    @Test
    fun `emitGuest emits Guest without clearing tokens`() =
        runTest {
            stateMachine.emitGuest()
            assertIs<AuthState.Guest>(stateMachine.state.value)
            // emitGuest does NOT clear tokens — only logout does
            coVerify(exactly = 0) { tokenStore.clearTokens() }
        }

    @Test
    fun `signIn emits Authenticated`() =
        runTest {
            stateMachine.signIn(
                userId = "user-2",
                email = "new@example.com",
                roles = setOf("client"),
                currentRole = "client",
            )
            val state = stateMachine.state.value
            assertIs<AuthState.Authenticated>(state)
            assertEquals("user-2", state.userId)
            assertEquals("new@example.com", state.email)
            assertEquals(setOf("client"), state.roles)
            assertEquals("client", state.currentRole)
        }
}
