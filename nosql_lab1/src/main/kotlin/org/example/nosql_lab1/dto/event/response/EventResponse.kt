package org.example.nosql_lab1.dto.event.response

import org.example.nosql_lab1.entity.enums.EventStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class EventResponse(
    val id: UUID, val title: String, val description: String, val eventDate: Instant,
    val location: String, val capacity: Int, val reservedSeats: Int, val availableSeats: Int,
    val price: BigDecimal, val status: EventStatus, val createdBy: UUID,
)
