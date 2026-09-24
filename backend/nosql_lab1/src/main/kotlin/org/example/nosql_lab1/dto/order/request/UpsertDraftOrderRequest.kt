package org.example.nosql_lab1.dto.order.request

import jakarta.validation.constraints.Positive

data class UpsertDraftOrderRequest(
    @field:Positive
    val quantity: Int,
)

