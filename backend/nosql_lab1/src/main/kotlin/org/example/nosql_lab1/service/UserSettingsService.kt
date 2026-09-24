package org.example.nosql_lab1.service

import org.example.nosql_lab1.dto.user_settings.request.PatchUserSettingsRequest
import org.example.nosql_lab1.dto.user_settings.request.UpsertUserSettingsRequest
import org.example.nosql_lab1.dto.user_settings.response.UserSettingsResponse
import org.example.nosql_lab1.entity.UserSettings
import org.example.nosql_lab1.repository.UserRepository
import org.example.nosql_lab1.repository.UserSettingsRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.ObjectMapper
import java.time.Duration
import java.util.UUID

@Service
class UserSettingsService(
    private val userSettingsRepository: UserSettingsRepository,
    private val userRepository: UserRepository,
    private val redis: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) {
    private fun cacheKey(userId: UUID) = "user-settings:v1:$userId"

    @Transactional(readOnly = true)
    fun getByUserId(userId: UUID): UserSettingsResponse {
        getFromCache(userId)?.let { return it }

        val settings = userSettingsRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "user settings not found :(")
        }.toResponse()

        putToCache(settings)
        return settings
    }

    @Transactional
    fun upsert(request: UpsertUserSettingsRequest): UserSettingsResponse {
        if (!userRepository.existsById(request.userId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        }

        val settings = userSettingsRepository.findById(request.userId)
            .orElse(UserSettings(userId = request.userId))

        settings.notificationsActive = request.notificationsActive
        settings.language = request.language.trim()
        settings.preferredCategory = request.preferredCategory?.trim()?.ifBlank { null }

        val response = userSettingsRepository.save(settings).toResponse()
        putToCacheAfterCommit(response)
        return response
    }

    private fun getFromCache(userId: UUID): UserSettingsResponse? {
        val value = redis.opsForValue().get(cacheKey(userId)) ?: return null

        return runCatching {
            objectMapper.readValue(value, UserSettingsResponse::class.java)
        }.getOrElse {
            redis.delete(cacheKey(userId))
            null
        }
    }

    /**
     * По результату коммита транзакции кэш настроек будет обновляться
     */
    private fun putToCacheAfterCommit(response: UserSettingsResponse) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            putToCache(response)
            return
        }

        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                putToCache(response)
            }
        })
    }

    private fun putToCache(response: UserSettingsResponse) {
        redis.opsForValue().set(
            cacheKey(response.userId),
            objectMapper.writeValueAsString(response),
            CACHE_TTL,
        )
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

    private companion object {
        val CACHE_TTL: Duration = Duration.ofMinutes(30)
    }
}
