package org.example.nosql_lab1.dto.event.response

/** Redis representation of a public catalogue page. */
data class CachedEventPage(
    val content: List<EventResponse>,
    val totalElements: Long,
)
