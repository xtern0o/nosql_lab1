package org.example.nosql_lab1.dto.order.request

import java.time.Instant
import java.util.UUID

data class DraftOrderDto(
    val eventId: UUID,
    val quantity: Int,
    val createdAt: Instant,
)
