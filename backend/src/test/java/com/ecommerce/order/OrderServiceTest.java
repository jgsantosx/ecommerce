package com.ecommerce.order;

import com.ecommerce.address.Address;
import com.ecommerce.address.AddressRequest;
import com.ecommerce.address.AddressService;
import com.ecommerce.cart.Cart;
import com.ecommerce.cart.CartRepository;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.product.Product;
import com.ecommerce.stock.StockService;
import com.ecommerce.user.User;
import com.ecommerce.user.UserRepository;
import com.ecommerce.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static com.ecommerce.support.TestEntities.product;
import static com.ecommerce.support.TestEntities.user;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private AddressService addressService;

    @Mock
    private StockService stockService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User customer;
    private Address address;
    private Product notebook;
    private Product mouse;

    @BeforeEach
    void setUp() {
        customer = user(1L, UserRole.CUSTOMER);
        notebook = product(10L, "Notebook", "4500.00");
        mouse = product(20L, "Mouse", "99.90");

        AddressRequest request = new AddressRequest();
        request.setRecipientName("João");
        request.setZipCode("01001-000");
        request.setStreet("Praça da Sé");
        request.setNumber("100");
        request.setDistrict("Sé");
        request.setCity("São Paulo");
        request.setState("sp");

        address = new Address(customer);
        address.apply(request);
    }

    private CheckoutRequest checkout(Long addressId) {
        CheckoutRequest request = new CheckoutRequest();
        request.setAddressId(addressId);
        return request;
    }

    @Test
    void shouldCreateOrderFreezingPricesDecreasingStockAndClearingCart() {
        Cart cart = new Cart(customer);
        // adicionados fora de ordem para verificar a ordenação dos locks
        cart.addItem(mouse, 2);
        cart.addItem(notebook, 1);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressService.getOwned(1L, 5L)).thenReturn(address);
        when(userRepository.getReferenceById(1L)).thenReturn(customer);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.checkout(1L, checkout(5L));

        assertEquals(OrderStatus.PENDING_PAYMENT, response.getStatus());
        assertEquals(new BigDecimal("4699.80"), response.getTotal());
        assertEquals("01001000", response.getShippingAddress().zipCode());
        assertEquals("SP", response.getShippingAddress().state());
        assertEquals(2, response.getItems().size());
        assertTrue(cart.isEmpty(), "carrinho deve ser limpo após o pedido");

        // Locks sempre na mesma ordem (id crescente) para evitar deadlock.
        InOrder inOrder = inOrder(stockService);
        inOrder.verify(stockService).decrease(10L, 1);
        inOrder.verify(stockService).decrease(20L, 2);
    }

    @Test
    void orderItemsKeepPriceEvenIfProductPriceChangesLater() {
        Order order = new Order(customer, ShippingAddress.copyOf(address));
        order.addItem(mouse, 1);

        mouse.setPrice(new BigDecimal("1.00"));

        assertEquals(new BigDecimal("99.90"), order.getItems().getFirst().getUnitPrice());
        assertEquals(new BigDecimal("99.90"), order.getTotal());
    }

    @Test
    void shouldRejectEmptyCart() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(new Cart(customer)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.checkout(1L, checkout(5L))
        );

        assertEquals("Carrinho vazio", exception.getMessage());
        verifyNoInteractions(stockService);
    }

    @Test
    void shouldNotSaveOrderWhenStockIsInsufficient() {
        Cart cart = new Cart(customer);
        cart.addItem(notebook, 1);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressService.getOwned(1L, 5L)).thenReturn(address);
        when(userRepository.getReferenceById(1L)).thenReturn(customer);
        when(stockService.decrease(10L, 1))
                .thenThrow(new BusinessException("Estoque insuficiente"));

        assertThrows(BusinessException.class, () -> orderService.checkout(1L, checkout(5L)));

        verify(orderRepository, never()).save(any());
        assertFalse(cart.isEmpty(), "carrinho não pode ser limpo se o pedido falhou");
    }

    @Test
    void shouldRejectInactiveProductAtCheckout() {
        Cart cart = new Cart(customer);
        cart.addItem(notebook, 1);
        notebook.setActive(false);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressService.getOwned(1L, 5L)).thenReturn(address);
        when(userRepository.getReferenceById(1L)).thenReturn(customer);

        assertThrows(BusinessException.class, () -> orderService.checkout(1L, checkout(5L)));
        verify(stockService, never()).decrease(anyLong(), anyInt());
    }

    @Test
    void customerCancelRestoresStock() {
        Order order = new Order(customer, ShippingAddress.copyOf(address));
        order.addItem(mouse, 2);
        order.addItem(notebook, 1);

        when(orderRepository.findWithLockById(7L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.cancelMine(1L, 7L);

        assertEquals(OrderStatus.CANCELED, response.getStatus());
        verify(stockService).increase(10L, 1);
        verify(stockService).increase(20L, 2);
    }

    @Test
    void customerCannotCancelPaidOrder() {
        Order order = new Order(customer, ShippingAddress.copyOf(address));
        order.addItem(mouse, 1);
        order.changeStatus(OrderStatus.PAID);

        when(orderRepository.findWithLockById(7L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> orderService.cancelMine(1L, 7L));
        verifyNoInteractions(stockService);
    }

    @Test
    void customerCannotTouchOrderOfAnotherUser() {
        Order order = new Order(user(2L, UserRole.CUSTOMER), ShippingAddress.copyOf(address));

        when(orderRepository.findWithLockById(7L)).thenReturn(Optional.of(order));

        assertThrows(ResourceNotFoundException.class, () -> orderService.cancelMine(1L, 7L));
    }

    @Test
    void adminCannotMakeInvalidTransition() {
        Order order = new Order(customer, ShippingAddress.copyOf(address));

        when(orderRepository.findWithLockById(7L)).thenReturn(Optional.of(order));

        assertThrows(
                BusinessException.class,
                () -> orderService.updateStatus(7L, OrderStatus.DELIVERED)
        );
    }
}
