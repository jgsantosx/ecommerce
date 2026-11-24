package com.ecommerce.stock;

import com.ecommerce.exception.BusinessException;
import com.ecommerce.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de unidade puro: sem Spring, sem banco, sem mocks.
 * As regras de estoque vivem na entidade, então dá para testá-las direto.
 */
class StockTest {

    private Stock stock;

    @BeforeEach
    void setUp() {
        Product product = new Product();
        product.setName("Notebook");
        stock = new Stock(product);
    }

    @Test
    void shouldStartWithZeroQuantity() {
        assertEquals(0, stock.getQuantity());
    }

    @Test
    void shouldIncreaseQuantity() {
        stock.increase(10);
        stock.increase(5);

        assertEquals(15, stock.getQuantity());
    }

    @Test
    void shouldDecreaseQuantity() {
        stock.increase(10);
        stock.decrease(4);

        assertEquals(6, stock.getQuantity());
    }

    @Test
    void shouldAllowDecreasingToExactlyZero() {
        stock.increase(3);
        stock.decrease(3);

        assertEquals(0, stock.getQuantity());
    }

    @Test
    void shouldRejectDecreaseBeyondAvailable() {
        stock.increase(2);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> stock.decrease(3)
        );

        assertTrue(exception.getMessage().contains("Estoque insuficiente"));
        assertEquals(2, stock.getQuantity(), "quantidade não pode mudar após falha");
    }

    @Test
    void shouldRejectNonPositiveAmounts() {
        assertThrows(BusinessException.class, () -> stock.increase(0));
        assertThrows(BusinessException.class, () -> stock.increase(-1));
        assertThrows(BusinessException.class, () -> stock.decrease(0));
    }
}
