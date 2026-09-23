package org.example.nosql_lab1.service

import org.example.nosql_lab1.dto.user_settings.request.PatchUserSettingsRequest
import org.example.nosql_lab1.dto.user_settings.request.UpsertUserSettingsRequest
import org.example.nosql_lab1.dto.user_settings.response.UserSettingsResponse
import org.example.nosql_lab1.entity.UserSettings
import org.example.nosql_lab1.repository.UserRepository
import org.example.nosql_lab1.repository.UserSettingsRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class UserSettingsService(
    private val userSettingsRepository: UserSettingsRepository,
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun getByUserId(userId: UUID): UserSettingsResponse =
        userSettingsRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "User settings not found")
        }.toResponse()

    @Transactional
    fun upsert(request: UpsertUserSettingsRequest): UserSettingsResponse {
        if (!userRepository.existsById(request.userId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }

        val settings = userSettingsRepository.findById(request.userId)
            .orElse(UserSettings(userId = request.userId))

        settings.notificationsActive = request.notificationsActive
        settings.language = request.language.trim()
        settings.preferredCategory = request.preferredCategory?.trim()?.ifBlank { null }

        return userSettingsRepository.save(settings).toResponse()
    }

    @Transactional
    fun patch(userId: UUID, request: PatchUserSettingsRequest): UserSettingsResponse {
        val settings = userSettingsRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "User settings not found")
        }

        request.notificationsActive?.let { settings.notificationsActive = it }
        request.language?.trim()?.takeIf { it.isNotBlank() }?.let { settings.language = it }

        when {
            request.clearPreferredCategory -> settings.preferredCategory = null
            request.preferredCategory != null ->
                settings.preferredCategory = request.preferredCategory.trim().ifBlank { null }
        }

        return settings.toResponse()
    }


    private fun UserSettings.toResponse() = UserSettingsResponse(
        userId = requireNotNull(userId),
        notificationsActive = notificationsActive,
        language = language,
        preferredCategory = preferredCategory,
    )
}
