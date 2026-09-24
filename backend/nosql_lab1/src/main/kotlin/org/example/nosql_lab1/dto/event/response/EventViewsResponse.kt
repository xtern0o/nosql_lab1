package org.example.nosql_lab1.dto.event.response

import java.util.UUID

data class EventViewsResponse(
    val eventId: UUID,
    val views: Long,
)
