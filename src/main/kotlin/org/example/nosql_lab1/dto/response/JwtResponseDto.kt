package org.example.nosql_lab1.dto.response

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class JwtResponseDto(
    @field:NotNull
    val token: String?,

    @field:NotNull
    val userId: UUID?,

    @field:NotNull
    val username: String?
)

