package org.example.nosql_lab1.entity

import java.math.BigDecimal
import java.util.UUID
import kotlin.time.Instant

data class OrderEntity(
    val id: UUID,
    val userId: UUID,
    val eventId: UUID,
    val quantity: Int,
    val totalPrice: BigDecimal,
    val createdAt: Instant,
    val status: OrderStatus
    )
