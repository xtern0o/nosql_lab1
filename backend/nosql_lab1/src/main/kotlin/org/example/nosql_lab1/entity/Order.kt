package org.example.nosql_lab1.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.example.nosql_lab1.entity.enums.OrderStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "orders")
class Order(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(nullable = false, updatable = false)
    var id: UUID? = null,

    @field:Column(name = "user_id", nullable = false, updatable = false)
    var userId: UUID? = null,

    @field:Column(name = "event_id", nullable = false, updatable = false)
    var eventId: UUID? = null,

    @field:Column(nullable = false)
    var quantity: Int = 0,

    @field:Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    var totalPrice: BigDecimal = BigDecimal.ZERO,

    @field:Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.EPOCH,

    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false, length = 32)
    var status: OrderStatus = OrderStatus.CREATED,
)
