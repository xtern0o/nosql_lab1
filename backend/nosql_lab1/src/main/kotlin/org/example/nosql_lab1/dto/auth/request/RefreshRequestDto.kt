package org.example.nosql_lab1.dto.auth.request

import jakarta.validation.constraints.NotBlank

data class RefreshRequestDto(
    @field:NotBlank(message = "refresh_token must not be blank")
    val refreshToken: String?,
)