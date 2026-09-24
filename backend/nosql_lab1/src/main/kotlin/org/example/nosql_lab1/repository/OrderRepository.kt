package org.example.nosql_lab1.repository

import org.example.nosql_lab1.entity.Order
import org.example.nosql_lab1.entity.enums.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    fun findByUserId(userId: UUID, pageable: Pageable): Page<Order>

    fun findByEventId(eventId: UUID, pageable: Pageable): Page<Order>

    fun existsByUserIdAndEventIdAndStatusIn(
        userId: UUID,
        eventId: UUID,
        statuses: Collection<OrderStatus>,
    ): Boolean
}
