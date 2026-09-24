package org.example.nosql_lab1.service

import org.example.nosql_lab1.config.KeycloakProperties
import org.example.nosql_lab1.dto.auth.request.AuthCodeRequestDto
import org.example.nosql_lab1.dto.auth.response.KeycloakTokenResponseDto
import org.example.nosql_lab1.dto.auth.response.TokenResponseDto
import org.example.nosql_lab1.dto.auth.response.toClientResponse
import org.springframework.http.MediaType
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

@Service
class KeycloakService(
    private val keycloakProperties: KeycloakProperties,
    private val webClientBuilder: WebClient.Builder,
) {
    fun exchangeCodeForTokens(authCodeRequestDto: AuthCodeRequestDto): TokenResponseDto {
        val formData = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("code", requireNotNull(authCodeRequestDto.code))
            add("redirect_uri", requireNotNull(authCodeRequestDto.redirectUri))
            add("client_id", keycloakProperties.clientId)
            add("client_secret", keycloakProperties.clientSecret)
        }

        return try {
            webClientBuilder.build()
                .post()
                .uri(keycloakProperties.tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(KeycloakTokenResponseDto::class.java)
                .block()
                ?.toClientResponse()
                ?: throw IllegalStateException("Token response from Keycloak is empty")
        } catch (exception: WebClientResponseException) {
            throw ResponseStatusException(
                exception.statusCode,
                "Keycloak rejected the token request: ${exception.responseBodyAsString}",
                exception,
            )
        } catch (exception: IllegalStateException) {
            throw ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Keycloak returned an invalid token response",
                exception,
            )
        }
    }

    fun refreshAccessToken(refreshToken: String): TokenResponseDto {
        require(refreshToken.isNotBlank()) { "Refresh token must not be blank" }

        val formData = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "refresh_token")
            add("refresh_token", refreshToken)
            add("client_id", keycloakProperties.clientId)
            add("client_secret", keycloakProperties.clientSecret)
        }

        return try {
            webClientBuilder.build()
                .post()
                .uri(keycloakProperties.tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(KeycloakTokenResponseDto::class.java)
                .block()
                ?.toClientResponse()
                ?: throw IllegalStateException("Token response from Keycloak is empty")
        } catch (exception: WebClientResponseException) {
            throw ResponseStatusException(
                exception.statusCode,
                "Keycloak rejected the token refresh request: ${exception.responseBodyAsString}",
                exception,
            )
        } catch (exception: IllegalStateException) {
            throw ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Keycloak returned an invalid token response",
                exception,
            )
        }
    }

    fun getConfig(): Map<String, String> {
        return mutableMapOf<String, String>().apply {
            put("keycloak_base_url", keycloakProperties.authServerUrl)
            put("client_id", keycloakProperties.clientId)
        }
    }
}
