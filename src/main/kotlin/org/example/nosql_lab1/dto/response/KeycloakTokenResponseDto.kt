package org.example.nosql_lab1.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * То, что мы получаем от КК при передаче Authorization Code
 */
data class KeycloakTokenResponseDto(
    val accessToken: String?,
    val expiresIn: String?,
    val refreshToken: String?,
    val refreshExpiresIn: String?,
    val tokenType: String?,
    val idToken: String?,
    val scope: String?,
    val notBeforePolicy: Int?,
    val sessionState: String?
)

fun KeycloakTokenResponseDto.toClientResponse(): TokenResponseDto {
    return TokenResponseDto(
        accessToken = this.accessToken ?: throw IllegalStateException("Access token is missing"),
        refreshToken = this.refreshToken ?: throw IllegalStateException("Refresh token is missing"),

        expiresIn = this.expiresIn?.toIntOrNull() ?: 300,
        refreshExpiresIn = this.refreshExpiresIn?.toIntOrNull() ?: 1800,

        tokenType = this.tokenType ?: "Bearer"
    )
}