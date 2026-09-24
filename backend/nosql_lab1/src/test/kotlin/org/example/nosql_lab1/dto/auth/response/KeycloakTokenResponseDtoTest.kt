package org.example.nosql_lab1.dto.auth.response

import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import kotlin.test.assertEquals

class KeycloakTokenResponseDtoTest {
    private val objectMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .build()

    @Test
    fun `maps keycloak snake case token response`() {
        val json = """
            {
              "access_token": "access-value",
              "expires_in": 300,
              "refresh_expires_in": 1800,
              "refresh_token": "refresh-value",
              "token_type": "Bearer",
              "id_token": "id-value",
              "session_state": "session-value",
              "scope": "openid profile email"
            }
        """.trimIndent()

        val response = objectMapper.readValue(json, KeycloakTokenResponseDto::class.java)
            .toClientResponse()

        assertEquals("access-value", response.accessToken)
        assertEquals("refresh-value", response.refreshToken)
        assertEquals(300, response.expiresIn)
        assertEquals(1800, response.refreshExpiresIn)
        assertEquals("id-value", response.idToken)
    }
}
