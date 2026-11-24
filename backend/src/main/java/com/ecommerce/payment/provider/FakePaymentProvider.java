package com.ecommerce.payment.provider;

import com.ecommerce.payment.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Provider de simulação, para desenvolvimento e testes. Regras:
 * - CREDIT_CARD com cardToken "tok_declined" → recusado
 * - BOLETO → pendente (no mundo real, confirmado depois via webhook)
 * - qualquer outro caso → aprovado
 */
@Component
public class FakePaymentProvider implements PaymentProvider {

    public static final String DECLINED_TOKEN = "tok_declined";

    @Override
    public String name() {
        return "fake";
    }

    @Override
    public PaymentResult process(PaymentCharge charge) {
        String reference = "fake_" + UUID.randomUUID();

        if (charge.method() == PaymentMethod.CREDIT_CARD
                && DECLINED_TOKEN.equals(charge.cardToken())) {
            return PaymentResult.declined(reference, "Cartão recusado (simulação)");
        }

        if (charge.method() == PaymentMethod.BOLETO) {
            return PaymentResult.pending(reference);
        }

        return PaymentResult.approved(reference);
    }
}
