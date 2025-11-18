package com.chayasadler.paymentservice.util;

import java.util.UUID;

public record OrderItemEvent(UUID productId,
                             Integer quantity) {
}
