package org.example.nosql_lab1.entity

import java.util.UUID

data class UserEntity(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val email: String,
    val role: UserRole,
)