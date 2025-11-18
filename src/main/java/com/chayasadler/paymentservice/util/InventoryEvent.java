package com.chayasadler.paymentservice.util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InventoryEvent(UUID messageId,
                             UUID orderId,
                             String customerId,
                             Double totalAmt,
                             List<OrderItemEvent> orderItemEventList,
                             String orderStatus,
                             String paymentStatus,
                             LocalDateTime timestamp) {
}
