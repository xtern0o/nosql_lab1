package org.example.nosql_lab1.repository

import org.example.nosql_lab1.entity.Event
import org.example.nosql_lab1.entity.enums.EventStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface EventRepository : JpaRepository<Event, UUID> {
    fun findByStatus(status: EventStatus, pageable: Pageable): Page<Event>

    fun findByStatusAndTitleContainingIgnoreCase(
        status: EventStatus,
        title: String,
        pageable: Pageable,
    ): Page<Event>
}
