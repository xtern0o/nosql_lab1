package org.example.nosql_lab1.controller

import org.example.nosql_lab1.dto.event.response.EventViewsResponse
import org.example.nosql_lab1.service.EventViewService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/events")
class EventViewController(
    private val eventViewService: EventViewService,
) {
    @PostMapping("/{eventId}/views")
    fun increment(@PathVariable eventId: UUID): EventViewsResponse =
        EventViewsResponse(
            eventId = eventId,
            views = eventViewService.increment(eventId),
        )

    @GetMapping("/{eventId}/views")
    fun getViews(@PathVariable eventId: UUID): EventViewsResponse =
        EventViewsResponse(
            eventId = eventId,
            views = eventViewService.getViews(eventId),
        )
}
