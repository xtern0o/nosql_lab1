package org.example.nosql_lab1.controller

import jakarta.validation.Valid
import org.example.nosql_lab1.dto.order.request.DraftOrderDto
import org.example.nosql_lab1.dto.order.request.UpsertDraftOrderRequest
import org.example.nosql_lab1.service.DraftOrderService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/orders/drafts")
class DraftOrderController(
    private val draftOrderService: DraftOrderService,
) {
    @PutMapping("/{userId}/{eventId}")
    fun upsert(
        @PathVariable userId: UUID,
        @PathVariable eventId: UUID,
        @Valid @RequestBody request: UpsertDraftOrderRequest,
    ): DraftOrderDto {
        val draft = DraftOrderDto(
            eventId = eventId,
            quantity = request.quantity,
            createdAt = Instant.now(),
        )

        draftOrderService.save(userId, eventId, draft)
        return draft
    }

    @GetMapping("/{userId}/{eventId}")
    fun get(
        @PathVariable userId: UUID,
        @PathVariable eventId: UUID,
    ): DraftOrderDto = draftOrderService.get(userId, eventId)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "draft order not found or expired :((((")
}
