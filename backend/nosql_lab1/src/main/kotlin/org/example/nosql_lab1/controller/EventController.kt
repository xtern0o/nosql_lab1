package org.example.nosql_lab1.controller

import jakarta.validation.Valid
import org.example.nosql_lab1.dto.event.request.EventSearchRequest
import org.example.nosql_lab1.dto.event.request.UpsertEventRequest
import org.example.nosql_lab1.dto.event.response.EventResponse
import org.example.nosql_lab1.service.EventService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/events")
class EventController(
    private val eventService: EventService,
) {
    @GetMapping
    fun findPublished(@Valid @ModelAttribute request: EventSearchRequest): Page<EventResponse> =
        eventService.findPublished(request)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): EventResponse = eventService.getById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: UpsertEventRequest): EventResponse =
        eventService.upsert(request.copy(id = null))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpsertEventRequest,
    ): EventResponse = eventService.upsert(request.copy(id = id))
}
