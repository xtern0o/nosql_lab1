package org.example.nosql_lab1.controller

import jakarta.validation.Valid
import org.example.nosql_lab1.dto.order.request.CreateOrderRequest
import org.example.nosql_lab1.dto.order.request.OrderPageRequest
import org.example.nosql_lab1.dto.order.request.UpdateOrderStatusRequest
import org.example.nosql_lab1.dto.order.response.OrderResponse
import org.example.nosql_lab1.service.OrderService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PatchMapping
import java.util.UUID

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateOrderRequest): OrderResponse =
        orderService.create(request)

    @GetMapping("/user/{userId}")
    fun findForUser(
        @PathVariable userId: UUID,
        @Valid @ModelAttribute request: OrderPageRequest,
    ): Page<OrderResponse> = orderService.findForUser(userId, request)

    @GetMapping("/event/{eventId}")
    fun findForEvent(
        @PathVariable eventId: UUID,
        @Valid @ModelAttribute request: OrderPageRequest,
    ): Page<OrderResponse> = orderService.findForEvent(eventId, request)

    @PatchMapping("/{id}/status")
    fun changeStatus(
        @PathVariable id: UUID,
        @RequestBody request: UpdateOrderStatusRequest,
    ): OrderResponse = orderService.changeStatus(request.copy(orderId = id))
}
