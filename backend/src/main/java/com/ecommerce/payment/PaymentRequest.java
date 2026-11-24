package com.ecommerce.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Sem campo "amount": o valor cobrado é sempre o total do pedido no banco. */
public class PaymentRequest {

    @NotNull(message = "Método de pagamento é obrigatório")
    private PaymentMethod method;

    @Size(max = 100, message = "Token do cartão deve ter no máximo 100 caracteres")
    private String cardToken;

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public String getCardToken() {
        return cardToken;
    }

    public void setCardToken(String cardToken) {
        this.cardToken = cardToken;
    }
}
