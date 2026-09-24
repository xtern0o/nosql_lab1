package org.example.nosql_lab1.controller

import jakarta.validation.Valid
import org.example.nosql_lab1.dto.user_settings.request.PatchUserSettingsRequest
import org.example.nosql_lab1.dto.user_settings.request.UpsertUserSettingsRequest
import org.example.nosql_lab1.dto.user_settings.response.UserSettingsResponse
import org.example.nosql_lab1.service.UserSettingsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/user-settings")
class UserSettingsController(
    private val userSettingsService: UserSettingsService,
) {
    @GetMapping("/{userId}")
    fun getByUserId(
        @PathVariable userId: UUID
    ): UserSettingsResponse =
        userSettingsService.getByUserId(userId)

    @PutMapping("/{userId}")
    fun upsert(
        @PathVariable userId: UUID,
        @Valid @RequestBody request: UpsertUserSettingsRequest,
    ): UserSettingsResponse = userSettingsService.upsert(request.copy(userId = userId))

    @PatchMapping("/{userId}")
    fun patch(
        @PathVariable userId: UUID,
        @Valid @RequestBody request: PatchUserSettingsRequest,
    ): UserSettingsResponse = userSettingsService.patch(userId, request)
}
