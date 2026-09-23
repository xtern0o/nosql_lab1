package org.example.nosql_lab1.dto.order.request

import org.example.nosql_lab1.entity.enums.OrderStatus
import java.util.UUID

data class UpdateOrderStatusRequest(
    val orderId: UUID,
    val status: OrderStatus,
)

