package com.ecommerce.payment;

import com.ecommerce.exception.BusinessException;
import com.ecommerce.order.Order;
import com.ecommerce.order.OrderService;
import com.ecommerce.order.OrderStatus;
import com.ecommerce.payment.provider.PaymentCharge;
import com.ecommerce.payment.provider.PaymentProvider;
import com.ecommerce.payment.provider.PaymentProviderResolver;
import com.ecommerce.payment.provider.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final PaymentProviderResolver providerResolver;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderService orderService,
            PaymentProviderResolver providerResolver
    ) {
        this.paymentRepository = paymentRepository;
        this.orderService = orderService;
        this.providerResolver = providerResolver;
    }

    /**
     * Observação de arquitetura: aqui chamamos o provider DENTRO da transação,
     * com o pedido travado. Com o provider fake isso é instantâneo. Com um
     * gateway real (chamada HTTP lenta), o ideal é separar em etapas: registrar
     * a tentativa, chamar o gateway fora da transação e confirmar via webhook.
     */
    @Transactional
    public PaymentResponse pay(Long userId, Long orderId, PaymentRequest request) {
        Order order = orderService.lockOwnedForPayment(userId, orderId);

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException("Pedido não está aguardando pagamento");
        }

        if (paymentRepository.existsByOrderIdAndStatus(orderId, PaymentStatus.PENDING)) {
            throw new BusinessException("Já existe um pagamento em processamento para este pedido");
        }

        if (request.getMethod() == PaymentMethod.CREDIT_CARD
                && (request.getCardToken() == null || request.getCardToken().isBlank())) {
            throw new BusinessException("Token do cartão é obrigatório para cartão de crédito");
        }

        PaymentProvider provider = providerResolver.active();

        PaymentResult result = provider.process(new PaymentCharge(
                order.getId(),
                order.getTotal(),
                request.getMethod(),
                request.getCardToken()
        ));

        Payment payment = paymentRepository.save(new Payment(
                order,
                request.getMethod(),
                order.getTotal(),
                provider.name(),
                result
        ));

        if (result.status() == PaymentStatus.APPROVED) {
            orderService.markPaid(order);
        }

        log.info("Pagamento {} do pedido {}: {}", payment.getId(), orderId, result.status());

        return PaymentResponse.fromEntity(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByOrder(Long userId, Long orderId) {
        // Reaproveita a checagem de dono (404 se o pedido não for do usuário).
        orderService.findMine(userId, orderId);

        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(PaymentResponse::fromEntity)
                .toList();
    }
}
