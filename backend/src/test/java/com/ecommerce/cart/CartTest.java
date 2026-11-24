package com.ecommerce.cart;

import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.product.Product;
import com.ecommerce.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.ecommerce.support.TestEntities.product;
import static com.ecommerce.support.TestEntities.user;
import static org.junit.jupiter.api.Assertions.*;

class CartTest {

    private Cart cart;
    private Product notebook;
    private Product mouse;

    @BeforeEach
    void setUp() {
        cart = new Cart(user(1L, UserRole.CUSTOMER));
        notebook = product(10L, "Notebook", "4500.00");
        mouse = product(20L, "Mouse", "99.90");
    }

    @Test
    void shouldAddItemsAndCalculateTotals() {
        cart.addItem(notebook, 1);
        cart.addItem(mouse, 2);

        assertEquals(2, cart.getItems().size());
        assertEquals(3, cart.getTotalItems());
        assertEquals(new BigDecimal("4699.80"), cart.getTotal());
    }

    @Test
    void shouldSumQuantityWhenAddingSameProductTwice() {
        cart.addItem(mouse, 1);
        cart.addItem(mouse, 2);

        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItem(20L).getQuantity());
    }

    @Test
    void shouldUseCurrentProductPrice() {
        cart.addItem(mouse, 2);

        mouse.setPrice(new BigDecimal("50.00"));

        assertEquals(new BigDecimal("100.00"), cart.getTotal());
    }

    @Test
    void shouldUpdateAndRemoveItems() {
        cart.addItem(notebook, 1);
        cart.addItem(mouse, 1);

        cart.updateItemQuantity(20L, 5);
        assertEquals(5, cart.getItem(20L).getQuantity());

        cart.removeItem(10L);
        assertTrue(cart.findItem(10L).isEmpty());
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void shouldRejectInvalidQuantityAndMissingItem() {
        assertThrows(BusinessException.class, () -> cart.addItem(mouse, 0));
        assertThrows(ResourceNotFoundException.class, () -> cart.removeItem(999L));
    }

    @Test
    void shouldClear() {
        cart.addItem(notebook, 1);
        cart.clear();

        assertTrue(cart.isEmpty());
        assertEquals(BigDecimal.ZERO, cart.getTotal());
    }
}
