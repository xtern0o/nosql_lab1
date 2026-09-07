package org.example.nosql_lab1.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class AuthCodeRequestDto(
    @field:NotBlank(message = "auth code should not be blank")
    val code: String?,

    @field:NotBlank(message = "redirect uri should not be blank")
    @field:JsonProperty("redirect_uri")
    val redirectUri: String?,
)