package org.example.nosql_lab1.dto.user_settings.response

import java.util.UUID

data class UserSettingsResponse(
    val userId: UUID, val notificationsActive: Boolean,
    val language: String, val preferredCategory: String?,
)
