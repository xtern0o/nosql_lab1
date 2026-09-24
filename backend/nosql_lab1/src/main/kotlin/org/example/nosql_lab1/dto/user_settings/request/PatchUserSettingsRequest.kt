package org.example.nosql_lab1.dto.user_settings.request

data class PatchUserSettingsRequest(
    val notificationsActive: Boolean? = null,
    val language: String? = null,
    val preferredCategory: String? = null,
    val clearPreferredCategory: Boolean = false,
)
