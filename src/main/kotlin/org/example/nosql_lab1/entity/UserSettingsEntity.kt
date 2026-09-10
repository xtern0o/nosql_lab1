package org.example.nosql_lab1.entity

import java.util.UUID

data class UserSettingsEntity(
    val userId: UUID,
    val notificationsActive: Boolean,
    val language: String,
    val preferredCategory: String?
)
