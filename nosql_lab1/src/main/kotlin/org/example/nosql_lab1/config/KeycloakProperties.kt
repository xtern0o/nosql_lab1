package org.example.nosql_lab1.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("keycloak")
data class KeycloakProperties(
    val authServerUrl: String,
    val internalAuthServerUrl: String,
    val clientSecret: String,
    val realm: String,
    val clientId: String,
) {
    val tokenUri: String
        get() = "$internalAuthServerUrl/realms/$realm/protocol/openid-connect/token"
}
