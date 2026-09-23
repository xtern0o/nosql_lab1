package org.example.nosql_lab1.dto.order.request

import jakarta.validation.constraints.Positive
import java.util.UUID

data class CreateOrderRequest(
    val userId: UUID,
    val eventId: UUID,

    @field:Positive
    val quantity: Int
)
