package org.example.nosql_lab1.dto.user.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.example.nosql_lab1.entity.enums.UserRole
import java.util.UUID

data class UpsertUserRequest(
    val id: UUID? = null,

    @field:NotBlank
    val name: String,

    @field:Email
    @field:NotBlank
    val email: String,

    val role: UserRole = UserRole.USER,
)
