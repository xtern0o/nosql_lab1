package org.example.nosql_lab1.dto.order.response

import org.example.nosql_lab1.entity.enums.OrderStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val userId: UUID,
    val eventId: UUID,
    val quantity: Int,
    val totalPrice: BigDecimal,
    val createdAt: Instant,
    val status: OrderStatus,
)
