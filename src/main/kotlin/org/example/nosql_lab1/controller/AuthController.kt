package org.example.nosql_lab1.controller

import jakarta.validation.Valid
import org.example.nosql_lab1.dto.request.AuthCodeRequestDto
import org.example.nosql_lab1.dto.request.RefreshRequestDto
import org.example.nosql_lab1.dto.response.TokenResponseDto
import org.example.nosql_lab1.service.KeycloakService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val keycloakService: KeycloakService
) {
    @GetMapping("/config")
    fun getAuthConfig(): Map<String, String> {
        return keycloakService.getConfig()
    }

    @PostMapping("/callback")
    fun exchangeCodeForTokens(
        @Valid @RequestBody authCodeRequestDto: AuthCodeRequestDto
    ): TokenResponseDto {
        return keycloakService.exchangeCodeForTokens(authCodeRequestDto)
    }

    @PostMapping("/refresh")
    fun refreshTokens(
        @Valid @RequestBody refreshRequestDto: RefreshRequestDto
    ): TokenResponseDto {
        return keycloakService.refreshAccessToken(refreshRequestDto.refreshToken!!)
    }

}