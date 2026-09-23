package org.example.nosql_lab1.service

import org.example.nosql_lab1.dto.order.request.CreateOrderRequest
import org.example.nosql_lab1.dto.order.request.OrderPageRequest
import org.example.nosql_lab1.dto.order.request.UpdateOrderStatusRequest
import org.example.nosql_lab1.dto.order.response.OrderResponse
import org.example.nosql_lab1.entity.Order
import org.example.nosql_lab1.entity.enums.EventStatus
import org.example.nosql_lab1.entity.enums.OrderStatus
import org.example.nosql_lab1.repository.EventRepository
import org.example.nosql_lab1.repository.OrderRepository
import org.example.nosql_lab1.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val eventListCache: EventListCacheService,
) {
    @Transactional
    fun create(request: CreateOrderRequest): OrderResponse {
        if (!userRepository.existsById(request.userId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        }

        val event = eventRepository.findById(request.eventId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "event not found")
        }

        if (event.status != EventStatus.PUBLISHED) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "event is not available")
        }

        if (orderRepository.existsByUserIdAndEventIdAndStatusIn(
                request.userId,
                request.eventId,
                listOf(OrderStatus.CREATED, OrderStatus.CONFIRMED),
            )
        ) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "active order for this event already exists",
            )
        }

        if (event.reservedSeats + request.quantity > event.capacity) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "not enough available seats")
        }

        event.reservedSeats += request.quantity

        val order = Order(
            userId = request.userId,
            eventId = request.eventId,
            quantity = request.quantity,
            totalPrice = event.price.multiply(BigDecimal.valueOf(request.quantity.toLong())),
            createdAt = Instant.now(),
            status = OrderStatus.CREATED,
        )

        val response = orderRepository.save(order).toResponse()
        eventListCache.invalidateAfterCommit()
        return response
    }

    @Transactional(readOnly = true)
    fun findForUser(userId: UUID, request: OrderPageRequest): Page<OrderResponse> =
        orderRepository.findByUserId(userId, PageRequest.of(request.page, request.size))
            .map { it.toResponse() }

    @Transactional(readOnly = true)
    fun findForEvent(eventId: UUID, request: OrderPageRequest): Page<OrderResponse> =
        orderRepository.findByEventId(eventId, PageRequest.of(request.page, request.size))
            .map { it.toResponse() }

    @Transactional
    fun changeStatus(request: UpdateOrderStatusRequest): OrderResponse {
        val order = orderRepository.findById(request.orderId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")
        }

        when (request.status) {
            OrderStatus.CONFIRMED -> {
                if (order.status != OrderStatus.CREATED) {
                    throw ResponseStatusException(HttpStatus.CONFLICT, "Order cannot be confirmed")
                }
                order.status = OrderStatus.CONFIRMED
            }

            OrderStatus.CANCELLED -> {
                if (order.status != OrderStatus.CANCELLED) {
                    val event = eventRepository.findById(requireNotNull(order.eventId)).orElseThrow {
                        ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found")
                    }

                    event.reservedSeats = (event.reservedSeats - order.quantity).coerceAtLeast(0)
                    order.status = OrderStatus.CANCELLED
                    eventListCache.invalidateAfterCommit()
                }
            }

            OrderStatus.CREATED -> {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order cannot be moved to CREATED",
                )
            }
        }

        return order.toResponse()
    }

    private fun Order.toResponse() = OrderResponse(
        id = requireNotNull(id),
        userId = requireNotNull(userId),
        eventId = requireNotNull(eventId),
        quantity = quantity,
        totalPrice = totalPrice,
        createdAt = createdAt,
        status = status,
    )
}
