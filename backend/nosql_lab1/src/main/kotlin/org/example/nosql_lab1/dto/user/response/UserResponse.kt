package org.example.nosql_lab1.dto.user.response

import org.example.nosql_lab1.entity.enums.UserRole
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val name: String,
    val email: String,
    val role: UserRole
)
