package com.ecommerce.payment;

public enum PaymentStatus {
    /** Aguardando confirmação do provider (ex.: boleto ainda não pago). */
    PENDING,
    APPROVED,
    DECLINED
}
