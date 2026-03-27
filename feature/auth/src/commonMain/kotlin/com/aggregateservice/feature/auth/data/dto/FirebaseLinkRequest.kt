package com.aggregateservice.feature.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для link Firebase account запроса (Data слой).
 *
 * Used to link an existing account with a Firebase credential.
 *
 * **Mapping:**
 * - Domain: LinkRequest → DTO: FirebaseLinkRequest (в Repository)
 * - Network: FirebaseLinkRequest → JSON (Ktor сериализация)
 *
 * @property tempToken Temporary token from FirebaseLinkRequiredResponse
 * @property password User's existing account password
 *
 * @see BACKEND_API_REFERENCE.md секция "Firebase Authentication"
 */
@Serializable
data class FirebaseLinkRequest(
    @SerialName("temp_token")
    val tempToken: String,
    val password: String,
)
