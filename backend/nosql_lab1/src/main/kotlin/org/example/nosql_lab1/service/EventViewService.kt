package org.example.nosql_lab1.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EventViewService(
    private val redis: StringRedisTemplate,
) {
    fun increment(eventId: UUID): Long =
        requireNotNull(
            redis.opsForValue().increment("event-views:v1:$eventId"),
        )

    fun getViews(eventId: UUID): Long =
        redis.opsForValue()
            .get("event-views:v1:$eventId")
            ?.toLongOrNull()
            ?: 0
}
