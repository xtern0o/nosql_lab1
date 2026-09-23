package org.example.nosql_lab1.dto.event.request

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.example.nosql_lab1.entity.enums.EventStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class UpsertEventRequest(
    val id: UUID? = null,
    @field:NotBlank val title: String,
    @field:NotBlank val description: String,
    val eventDate: Instant,
    @field:NotBlank val location: String,
    @field:Positive val capacity: Int,
    @field:DecimalMin("0.00") val price: BigDecimal,
    val createdBy: UUID? = null,
    val status: EventStatus? = null,
)
