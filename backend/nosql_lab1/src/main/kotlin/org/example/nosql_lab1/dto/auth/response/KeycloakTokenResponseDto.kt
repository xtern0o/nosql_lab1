package org.example.nosql_lab1.dto.auth.response

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * То, что мы получаем от КК при передаче Authorization Code
 */
data class KeycloakTokenResponseDto(
    @param:JsonProperty("access_token")
    val accessToken: String? = null,
    @param:JsonProperty("expires_in")
    val expiresIn: Int? = null,
    @param:JsonProperty("refresh_token")
    val refreshToken: String? = null,
    @param:JsonProperty("refresh_expires_in")
    val refreshExpiresIn: Int? = null,
    @param:JsonProperty("token_type")
    val tokenType: String? = null,
    @param:JsonProperty("id_token")
    val idToken: String? = null,
    val scope: String? = null,
    @param:JsonProperty("not-before-policy")
    val notBeforePolicy: Int? = null,
    @param:JsonProperty("session_state")
    val sessionState: String? = null,
)

fun KeycloakTokenResponseDto.toClientResponse(): TokenResponseDto {
    return TokenResponseDto(
        accessToken = this.accessToken ?: throw IllegalStateException("Access token is missing"),
        refreshToken = this.refreshToken ?: throw IllegalStateException("Refresh token is missing"),
        expiresIn = this.expiresIn ?: 300,
        refreshExpiresIn = this.refreshExpiresIn ?: 1800,
        tokenType = this.tokenType ?: "Bearer",
        idToken = this.idToken,
    )
}
