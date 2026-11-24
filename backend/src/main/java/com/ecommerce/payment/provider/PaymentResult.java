package com.ecommerce.payment.provider;

import com.ecommerce.payment.PaymentStatus;

/** O que o provider responde, já traduzido para o vocabulário do nosso domínio. */
public record PaymentResult(
        PaymentStatus status,
        String providerReference,
        String failureReason
) {

    public static PaymentResult approved(String reference) {
        return new PaymentResult(PaymentStatus.APPROVED, reference, null);
    }

    public static PaymentResult pending(String reference) {
        return new PaymentResult(PaymentStatus.PENDING, reference, null);
    }

    public static PaymentResult declined(String reference, String reason) {
        return new PaymentResult(PaymentStatus.DECLINED, reference, reason);
    }
}
