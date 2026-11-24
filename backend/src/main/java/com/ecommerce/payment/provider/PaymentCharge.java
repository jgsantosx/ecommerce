package com.ecommerce.payment.provider;

import com.ecommerce.payment.PaymentMethod;

import java.math.BigDecimal;

/**
 * O que enviamos ao provider. "record" é uma classe imutável compacta do Java:
 * o compilador gera construtor, getters (orderId(), amount()...), equals e toString.
 *
 * cardToken: em gateways reais o número do cartão NUNCA passa pelo nosso servidor.
 * O frontend envia o cartão direto ao gateway e recebe um token, que é o que chega aqui.
 */
public record PaymentCharge(
        Long orderId,
        BigDecimal amount,
        PaymentMethod method,
        String cardToken
) {
}
