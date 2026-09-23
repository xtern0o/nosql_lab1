package org.example.nosql_lab1.dto.event.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class EventSearchRequest(
    val title: String? = null,

    @field:Min(0)
    val page: Int = 0,

    @field:Min(1)
    @field:Max(100)
    val size: Int = 20,
)
