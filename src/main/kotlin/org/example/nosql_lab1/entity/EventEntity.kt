package org.example.nosql_lab1.entity

import java.math.BigDecimal
import java.util.UUID
import kotlin.time.Instant

data class EventEntity(
    val id: UUID,
    val title: String,
    val description: String,
    val eventDate: Instant,
    val location: String,
    val capacity: Int,
    val price: BigDecimal,
    val status: EventStatus,
    val createdBy: UUID
)