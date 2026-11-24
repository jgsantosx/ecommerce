package com.ecommerce.stock;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.product.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockService stockService;

    @Test
    void shouldInitializeStockWithZero() {
        Product product = new Product();

        stockService.initialize(product);

        ArgumentCaptor<Stock> captor = ArgumentCaptor.forClass(Stock.class);
        verify(stockRepository).save(captor.capture());

        assertSame(product, captor.getValue().getProduct());
        assertEquals(0, captor.getValue().getQuantity());
    }

    @Test
    void shouldIncreaseUsingLockedRow() {
        Stock stock = new Stock(new Product());

        when(stockRepository.findByProductIdForUpdate(1L))
                .thenReturn(Optional.of(stock));

        StockResponse response = stockService.increase(1L, 7);

        assertEquals(7, response.getQuantity());
        verify(stockRepository).findByProductIdForUpdate(1L);
    }

    @Test
    void shouldReturnZeroAvailableWhenStockDoesNotExist() {
        when(stockRepository.findByProductId(99L))
                .thenReturn(Optional.empty());

        assertEquals(0, stockService.availableQuantity(99L));
    }

    @Test
    void shouldThrowNotFoundWhenStockDoesNotExist() {
        when(stockRepository.findByProductIdForUpdate(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> stockService.decrease(99L, 1)
        );
    }
}
