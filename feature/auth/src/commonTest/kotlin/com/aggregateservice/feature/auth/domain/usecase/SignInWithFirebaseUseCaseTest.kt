package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.core.firebase.FirebaseAuthApi
import com.aggregateservice.core.firebase.FirebaseToken
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignInWithFirebaseUseCaseTest {
    private lateinit var useCase: SignInWithFirebaseUseCase
    private lateinit var mockFirebaseAuthApi: FirebaseAuthApi
    private lateinit var mockRepository: AuthRepository

    @Test
    fun `should delegate sign-in to firebase and verify with repository`() = runTest {
        // given
        mockFirebaseAuthApi = mockk(relaxed = true)
        mockRepository = mockk(relaxed = true)
        useCase = SignInWithFirebaseUseCase(mockFirebaseAuthApi, mockRepository)

        val firebaseToken = FirebaseToken("idToken", "google.com")
        val authState = AuthState.Authenticated("token", "userId", "email@test.com")
        coEvery { mockFirebaseAuthApi.signInWithGoogle() } returns Result.success(firebaseToken)
        coEvery { mockRepository.verifyFirebaseToken("google.com", "idToken") } returns Result.success(authState)

        // when
        val result = useCase()

        // then
        assertTrue(result.isSuccess)
        assertEquals(authState, result.getOrNull())
        coVerify { mockFirebaseAuthApi.signInWithGoogle() }
        coVerify { mockRepository.verifyFirebaseToken("google.com", "idToken") }
    }

    @Test
    fun `should propagate firebase error`() = runTest {
        // given
        mockFirebaseAuthApi = mockk(relaxed = true)
        mockRepository = mockk(relaxed = true)
        useCase = SignInWithFirebaseUseCase(mockFirebaseAuthApi, mockRepository)

        coEvery { mockFirebaseAuthApi.signInWithGoogle() } returns Result.failure(Exception("Firebase error"))

        // when
        val result = useCase()

        // then
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { mockRepository.verifyFirebaseToken(any(), any()) }
    }

    @Test
    fun `should propagate repository error`() = runTest {
        // given
        mockFirebaseAuthApi = mockk(relaxed = true)
        mockRepository = mockk(relaxed = true)
        useCase = SignInWithFirebaseUseCase(mockFirebaseAuthApi, mockRepository)

        val firebaseToken = FirebaseToken("idToken", "google.com")
        coEvery { mockFirebaseAuthApi.signInWithGoogle() } returns Result.success(firebaseToken)
        coEvery { mockRepository.verifyFirebaseToken("google.com", "idToken") } returns Result.failure(AppError.Unauthorized)

        // when
        val result = useCase()

        // then
        assertTrue(result.isFailure)
    }
}