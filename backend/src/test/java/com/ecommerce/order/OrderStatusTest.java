package com.ecommerce.order;

import org.junit.jupiter.api.Test;

import static com.ecommerce.order.OrderStatus.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    void shouldAllowHappyPath() {
        assertTrue(PENDING_PAYMENT.canTransitionTo(PAID));
        assertTrue(PAID.canTransitionTo(SHIPPED));
        assertTrue(SHIPPED.canTransitionTo(DELIVERED));
    }

    @Test
    void shouldAllowCancelOnlyBeforeShipping() {
        assertTrue(PENDING_PAYMENT.canTransitionTo(CANCELED));
        assertTrue(PAID.canTransitionTo(CANCELED));
        assertFalse(SHIPPED.canTransitionTo(CANCELED));
        assertFalse(DELIVERED.canTransitionTo(CANCELED));
    }

    @Test
    void shouldNotSkipStepsOrGoBack() {
        assertFalse(PENDING_PAYMENT.canTransitionTo(SHIPPED));
        assertFalse(PENDING_PAYMENT.canTransitionTo(DELIVERED));
        assertFalse(SHIPPED.canTransitionTo(PAID));
    }

    @Test
    void finalStatesHaveNoTransitions() {
        assertTrue(DELIVERED.allowedNext().isEmpty());
        assertTrue(CANCELED.allowedNext().isEmpty());
    }
}
