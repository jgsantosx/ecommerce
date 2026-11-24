package com.ecommerce.payment;

import com.ecommerce.exception.BusinessException;
import com.ecommerce.order.Order;
import com.ecommerce.order.OrderService;
import com.ecommerce.order.OrderStatus;
import com.ecommerce.payment.provider.FakePaymentProvider;
import com.ecommerce.payment.provider.PaymentProviderResolver;
import com.ecommerce.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.ecommerce.support.TestEntities.product;
import static com.ecommerce.support.TestEntities.user;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderService orderService;

    private PaymentService paymentService;

    private Order order;

    @BeforeEach
    void setUp() {
        // Provider real (fake), escolhido pelo resolver como em produção.
        PaymentProviderResolver resolver =
                new PaymentProviderResolver(List.of(new FakePaymentProvider()), "fake");

        paymentService = new PaymentService(paymentRepository, orderService, resolver);

        order = new Order(user(1L, UserRole.CUSTOMER), null);
        order.addItem(product(10L, "Notebook", "4500.00"), 1);
    }

    private PaymentRequest request(PaymentMethod method, String token) {
        PaymentRequest request = new PaymentRequest();
        request.setMethod(method);
        request.setCardToken(token);
        return request;
    }

    @Test
    void approvedPaymentMarksOrderAsPaidAndChargesOrderTotal() {
        when(orderService.lockOwnedForPayment(1L, 7L)).thenReturn(order);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.pay(1L, 7L, request(PaymentMethod.PIX, null));

        assertEquals(PaymentStatus.APPROVED, response.status());
        assertEquals(order.getTotal(), response.amount());
        verify(orderService).markPaid(order);
    }

    @Test
    void declinedPaymentKeepsOrderPending() {
        when(orderService.lockOwnedForPayment(1L, 7L)).thenReturn(order);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.pay(
                1L, 7L, request(PaymentMethod.CREDIT_CARD, FakePaymentProvider.DECLINED_TOKEN)
        );

        assertEquals(PaymentStatus.DECLINED, response.status());
        assertNotNull(response.failureReason());
        verify(orderService, never()).markPaid(any());
    }

    @Test
    void shouldRejectPaymentForOrderNotPending() {
        order.changeStatus(OrderStatus.PAID);
        when(orderService.lockOwnedForPayment(1L, 7L)).thenReturn(order);

        assertThrows(
                BusinessException.class,
                () -> paymentService.pay(1L, 7L, request(PaymentMethod.PIX, null))
        );
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void creditCardRequiresToken() {
        when(orderService.lockOwnedForPayment(1L, 7L)).thenReturn(order);

        assertThrows(
                BusinessException.class,
                () -> paymentService.pay(1L, 7L, request(PaymentMethod.CREDIT_CARD, " "))
        );
    }

    @Test
    void unknownProviderFailsAtStartup() {
        assertThrows(
                IllegalStateException.class,
                () -> new PaymentProviderResolver(List.of(new FakePaymentProvider()), "stripe")
        );
    }
}
