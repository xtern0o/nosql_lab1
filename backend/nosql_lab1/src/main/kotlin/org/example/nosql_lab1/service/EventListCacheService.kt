package org.example.nosql_lab1.service

import org.example.nosql_lab1.dto.event.response.CachedEventPage
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import tools.jackson.databind.ObjectMapper
import java.time.Duration

@Service
class EventListCacheService(
    private val redis: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) {
    fun getFirstPage(): CachedEventPage? {
        val value = redis.opsForValue().get(CACHE_KEY) ?: return null

        return runCatching {
            objectMapper.readValue(value, CachedEventPage::class.java)
        }.getOrElse {
            redis.delete(CACHE_KEY)
            null
        }
    }

    fun putFirstPage(page: CachedEventPage) {
        redis.opsForValue().set(CACHE_KEY, objectMapper.writeValueAsString(page), CACHE_TTL)
    }

    fun invalidateAfterCommit() {
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                redis.delete(CACHE_KEY)
            }
        })
    }

    private companion object {
        const val CACHE_KEY = "event-list:v1:first-page"
        val CACHE_TTL: Duration = Duration.ofMinutes(2)
    }
}
