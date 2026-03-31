package com.aggregateservice.feature.auth.data.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator
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
 * Sealed interface для polymorphic deserialization Firebase verify responses.
 *
 * Backend возвращает либо FirebaseAlreadyLinkedResponse либо FirebaseLinkRequiredResponse.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("response_type")
@Serializable
sealed interface FirebaseVerifyResponse

/**
 * DTO для ответа when Firebase token is valid but account linking is required.
 *
 * This response indicates that the Firebase credential is valid, but the
 * Firebase account is not yet linked to an existing account in the system.
 *
 * @property email Email of the existing account to link with
 * @property firebaseUid Firebase UID of the credential
 * @property provider Auth provider (google, apple, phone)
 * @property message User-friendly message (optional)
 */
@Serializable
@SerialName("link_required")
data class FirebaseLinkRequiredResponse(
    val email: String,
    @SerialName("firebase_uid")
    val firebaseUid: String,
    val provider: String,
    val message: String? = null,
) : FirebaseVerifyResponse

/**
 * DTO for user object in Firebase verify responses.
 *
 * @property id User UUID from backend
 * @property email User email address
 * @property isActive Whether user account is active
 * @property isVerified Whether user has verified their email
 * @property roles List of user roles
 * @property currentRole Current active role
 */
@Serializable
data class FirebaseUserResponse(
    val id: String,
    val email: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("is_verified")
    val isVerified: Boolean = true,
    val roles: List<String> = emptyList(),
    @SerialName("current_role")
    val currentRole: String? = null,
)

/**
 * DTO для ответа when Firebase account is already linked.
 *
 * This response indicates that the Firebase credential is valid and
 * the account is already linked.
 *
 * @property accessToken JWT access token
 * @property message User-friendly message (optional)
 * @property user User data from backend
 */
@Serializable
@SerialName("already_linked")
data class FirebaseAlreadyLinkedResponse(
    @SerialName("access_token")
    val accessToken: String,
    val message: String? = null,
    val user: FirebaseUserResponse,
) : FirebaseVerifyResponse
