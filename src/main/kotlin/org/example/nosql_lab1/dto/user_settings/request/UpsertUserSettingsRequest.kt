package org.example.nosql_lab1.dto.user_settings.request

import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class UpsertUserSettingsRequest(
    val userId: UUID,
    val notificationsActive: Boolean,

    @field:NotBlank
    val language: String,

    val preferredCategory: String?,
)
