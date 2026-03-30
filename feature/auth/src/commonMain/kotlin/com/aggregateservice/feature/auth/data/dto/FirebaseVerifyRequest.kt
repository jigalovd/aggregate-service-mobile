package com.aggregateservice.feature.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
@Serializable(FirebaseVerifyResponseSerializer::class)
sealed interface FirebaseVerifyResponse

/**
 * DTO для ответа when Firebase token is valid but account linking is required.
 *
 * This response indicates that the Firebase credential is valid, but the
 * Firebase account is not yet linked to an existing account in the system.
 *
 * @property tempToken Temporary token to complete account linking
 * @property email Email of the existing account to link with
 * @property firebaseUid Firebase UID of the credential
 * @property message User-friendly message
 */
@Serializable
data class FirebaseLinkRequiredResponse(
    @SerialName("temp_token")
    val tempToken: String,
    val email: String,
    @SerialName("firebase_uid")
    val firebaseUid: String,
    val message: String,
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
 * Custom serializer for FirebaseVerifyResponse sealed interface.
 *
 * Backend does not include a discriminator field, so we detect the response type
 * by checking for the presence of specific fields:
 * - If "user" field exists -> FirebaseAlreadyLinkedResponse
 * - If "temp_token" field exists -> FirebaseLinkRequiredResponse
 *
 * This avoids requiring backend API changes.
 */
object FirebaseVerifyResponseSerializer : JsonContentPolymorphicSerializer<FirebaseVerifyResponse>(FirebaseVerifyResponse::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out FirebaseVerifyResponse> {
        val jsonObject = element.jsonObject
        return when {
            jsonObject.containsKey("user") -> FirebaseAlreadyLinkedResponse.serializer()
            jsonObject.containsKey("temp_token") -> FirebaseLinkRequiredResponse.serializer()
            else -> throw IllegalArgumentException("Unknown FirebaseVerifyResponse type: ${element}")
        }
    }
}

/**
 * DTO для ответа when Firebase account is already linked.
 *
 * This response indicates that the Firebase credential is valid and
 * the account is already linked.
 *
 * @property accessToken JWT access token
 * @property message User-friendly message
 * @property user User data from backend
 */
@Serializable
data class FirebaseAlreadyLinkedResponse(
    @SerialName("access_token")
    val accessToken: String,
    val message: String,
    val user: FirebaseUserResponse,
) : FirebaseVerifyResponse
