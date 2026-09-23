package org.example.nosql_lab1.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.example.nosql_lab1.entity.enums.EventStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "events")
class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    var id: UUID? = null,

    @Column(nullable = false, length = 255)
    var title: String = "",

    @Column(nullable = false, columnDefinition = "text")
    var description: String = "",

    @Column(name = "event_date", nullable = false)
    var eventDate: Instant = Instant.EPOCH,

    @Column(nullable = false, length = 255)
    var location: String = "",

    @Column(nullable = false)
    var capacity: Int = 0,

    @Column(nullable = false, precision = 12, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: EventStatus = EventStatus.DRAFTED,

    @Column(name = "created_by", nullable = false, updatable = false)
    var createdBy: UUID? = null,
)
