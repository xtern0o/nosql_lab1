package org.example.nosql_lab1.service

import org.example.nosql_lab1.dto.event.request.EventSearchRequest
import org.example.nosql_lab1.dto.event.request.UpsertEventRequest
import org.example.nosql_lab1.dto.event.response.CachedEventPage
import org.example.nosql_lab1.dto.event.response.EventResponse
import org.example.nosql_lab1.entity.Event
import org.example.nosql_lab1.entity.enums.EventStatus
import org.example.nosql_lab1.repository.EventRepository
import org.example.nosql_lab1.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class EventService(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val eventListCache: EventListCacheService,
) {
    @Transactional(readOnly = true)
    fun getById(id: UUID): EventResponse =
        findEvent(id).toResponse()

    @Transactional(readOnly = true)
    fun findPublished(request: EventSearchRequest): Page<EventResponse> {
        val title = request.title?.trim().orEmpty()
        val useCache = request.page == FIRST_PAGE && request.size == FIRST_PAGE_SIZE && title.isBlank()

        if (useCache) {
            eventListCache.getFirstPage()?.let {
                return PageImpl(it.content, PageRequest.of(FIRST_PAGE, FIRST_PAGE_SIZE), it.totalElements)
            }
        }

        val pageable = PageRequest.of(request.page, request.size)

        val events = if (title.isBlank()) {
            eventRepository.findByStatus(EventStatus.PUBLISHED, pageable)
        } else {
            eventRepository.findByStatusAndTitleContainingIgnoreCase(
                EventStatus.PUBLISHED,
                title,
                pageable,
            )
        }

        val response = events.map { it.toResponse() }

        if (useCache) {
            eventListCache.putFirstPage(
                CachedEventPage(response.content, response.totalElements),
            )
        }

        return response
    }

    @Transactional
    fun upsert(request: UpsertEventRequest): EventResponse {
        val event = if (request.id == null) {
            val createdBy = requireNotNull(request.createdBy) {
                "createdBy is required when creating an event"
            }

            if (!userRepository.existsById(createdBy)) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "manager not found")
            }

            Event(
                createdBy = createdBy,
                status = request.status ?: EventStatus.DRAFTED,
            )
        } else {
            findEvent(request.id)
        }

        if (request.capacity < event.reservedSeats) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "capacity cannot be less than reserved seats",
            )
        }

        event.title = request.title.trim()
        event.description = request.description.trim()
        event.eventDate = request.eventDate
        event.location = request.location.trim()
        event.capacity = request.capacity
        event.price = request.price
        request.status?.let { event.status = it }

        val response = eventRepository.save(event).toResponse()
        eventListCache.invalidateAfterCommit()
        return response
    }

    private fun findEvent(id: UUID): Event =
        eventRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "event not found")
        }

    private fun Event.toResponse() = EventResponse(
        id = requireNotNull(id),
        title = title,
        description = description,
        eventDate = eventDate,
        location = location,
        capacity = capacity,
        reservedSeats = reservedSeats,
        availableSeats = capacity - reservedSeats,
        price = price,
        status = status,
        createdBy = requireNotNull(createdBy),
    )

    private companion object {
        const val FIRST_PAGE = 0
        const val FIRST_PAGE_SIZE = 20
    }
}
