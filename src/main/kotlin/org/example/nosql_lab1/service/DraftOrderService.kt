package org.example.nosql_lab1.service

import org.example.nosql_lab1.dto.order.request.DraftOrderDto
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.time.Duration
import java.util.UUID

@Service
class DraftOrderService(
    private val redis: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) {
    fun save(userId: UUID, eventId: UUID, draft: DraftOrderDto) {
        redis.opsForValue().set(
            "order-draft:v1:$userId:$eventId",
            objectMapper.writeValueAsString(draft),
            Duration.ofMinutes(15),
        )
    }

    fun get(userId: UUID, eventId: UUID): DraftOrderDto? =
        redis.opsForValue()
            .get("order-draft:v1:$userId:$eventId")
            ?.let {
                objectMapper.readValue(
                    it,
                    DraftOrderDto::class.java
                )
            }
}
