package com.aggregateservice.feature.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для verify Firebase token запроса (Data слой).
 *
 * **Mapping:**
 * - Domain: FirebaseToken → DTO: FirebaseVerifyRequest (в Repository)
 * - Network: FirebaseVerifyRequest → JSON (Ktor сериализация)
 *
 * @property authProvider Firebase auth provider (google, apple, phone)
 * @property firebaseToken Firebase ID token from client
 *
 * @see BACKEND_API_REFERENCE.md секция "Firebase Authentication"
 */
@Serializable
data class FirebaseVerifyRequest(
    @SerialName("auth_provider")
    val authProvider: String,
    @SerialName("firebase_token")
    val firebaseToken: String,
)

/**
 * DTO для ответа when Firebase token is valid but account linking is required.
 *
 * This response indicates that the Firebase credential is valid, but the
 * Firebase account is not yet linked to an existing account in the system.
 *
 * @property tempToken Temporary token to complete account linking
 * @property message User-friendly message
 */
@Serializable
data class FirebaseLinkRequiredResponse(
    @SerialName("temp_token")
    val tempToken: String,
    val message: String,
)

/**
 * DTO для ответа when Firebase account is already linked.
 *
 * This response indicates that the Firebase credential is valid and
 * the account is already linked.
 *
 * @property accessToken JWT access token
 * @property message User-friendly message
 */
@Serializable
data class FirebaseAlreadyLinkedResponse(
    @SerialName("access_token")
    val accessToken: String,
    val message: String,
)
