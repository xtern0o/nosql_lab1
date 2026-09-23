package org.example.nosql_lab1.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.example.nosql_lab1.entity.enums.EventStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "events")
class Event(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(nullable = false, updatable = false)
    var id: UUID? = null,

    @field:Column(nullable = false, length = 255)
    var title: String = "",

    @field:Column(nullable = false, columnDefinition = "text")
    var description: String = "",

    @field:Column(name = "event_date", nullable = false)
    var eventDate: Instant = Instant.EPOCH,

    @field:Column(nullable = false, length = 255)
    var location: String = "",

    @field:Column(nullable = false)
    var capacity: Int = 0,

    @field:Column(name = "reserved_seats", nullable = false)
    var reservedSeats: Int = 0,

    @field:Column(nullable = false, precision = 12, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO,

    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false, length = 32)
    var status: EventStatus = EventStatus.DRAFTED,

    @field:Column(name = "created_by", nullable = false, updatable = false)
    var createdBy: UUID? = null,

    @field:Version
    var version: Long? = null,
)
