package com.ecommerce.cart;

import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.product.Product;
import com.ecommerce.product.ProductRepository;
import com.ecommerce.stock.StockService;
import com.ecommerce.user.UserRepository;
import com.ecommerce.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.ecommerce.support.TestEntities.product;
import static com.ecommerce.support.TestEntities.user;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockService stockService;

    @InjectMocks
    private CartService cartService;

    private CartItemRequest request(Long productId, int quantity) {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);
        return request;
    }

    @Test
    void shouldCreateCartOnFirstItem() {
        Product mouse = product(20L, "Mouse", "99.90");

        when(productRepository.findById(20L)).thenReturn(Optional.of(mouse));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(1L)).thenReturn(user(1L, UserRole.CUSTOMER));
        when(stockService.availableQuantity(20L)).thenReturn(10);

        CartResponse response = cartService.addItem(1L, request(20L, 2));

        assertEquals(2, response.getTotalItems());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldRejectWhenQuantityExceedsStockConsideringItemsAlreadyInCart() {
        Product mouse = product(20L, "Mouse", "99.90");
        Cart cart = new Cart(user(1L, UserRole.CUSTOMER));
        cart.addItem(mouse, 3);

        when(productRepository.findById(20L)).thenReturn(Optional.of(mouse));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(stockService.availableQuantity(20L)).thenReturn(4);

        // 3 já no carrinho + 2 = 5 > 4 disponíveis
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.addItem(1L, request(20L, 2))
        );

        assertTrue(exception.getMessage().contains("Disponível: 4"));
        assertEquals(3, cart.getItem(20L).getQuantity());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void shouldRejectInactiveProduct() {
        Product mouse = product(20L, "Mouse", "99.90");
        mouse.setActive(false);

        when(productRepository.findById(20L)).thenReturn(Optional.of(mouse));

        assertThrows(
                BusinessException.class,
                () -> cartService.addItem(1L, request(20L, 1))
        );
    }

    @Test
    void shouldReturn404WhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addItem(1L, request(99L, 1))
        );
    }

    @Test
    void shouldReturnEmptyCartWhenUserHasNone() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        CartResponse response = cartService.getCart(1L);

        assertTrue(response.getItems().isEmpty());
        assertEquals(0, response.getTotalItems());
    }
}
