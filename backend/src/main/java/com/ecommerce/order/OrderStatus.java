package com.ecommerce.order;

import java.util.Set;

/**
 * Máquina de estados do pedido. Cada estado sabe para quais outros pode ir:
 *
 * PENDING_PAYMENT ──► PAID ──► SHIPPED ──► DELIVERED
 *        │             │
 *        └──► CANCELED ◄┘
 */
public enum OrderStatus {

    PENDING_PAYMENT,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELED;

    public Set<OrderStatus> allowedNext() {
        return switch (this) {
            case PENDING_PAYMENT -> Set.of(PAID, CANCELED);
            case PAID -> Set.of(SHIPPED, CANCELED);
            case SHIPPED -> Set.of(DELIVERED);
            case DELIVERED, CANCELED -> Set.of();
        };
    }

    public boolean canTransitionTo(OrderStatus next) {
        return allowedNext().contains(next);
    }
}
