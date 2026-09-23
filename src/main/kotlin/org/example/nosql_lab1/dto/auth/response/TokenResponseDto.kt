package org.example.nosql_lab1.dto.auth.response

/**
 * Очищенный ответ для нашего фронтенда
 */
data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val refreshExpiresIn: Int,
    val tokenType: String = "Bearer"
)