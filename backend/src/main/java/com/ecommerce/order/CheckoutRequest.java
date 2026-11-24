package com.ecommerce.order;

import jakarta.validation.constraints.NotNull;

/**
 * Repare no que NÃO está aqui: itens, preços e total. Tudo isso vem do
 * carrinho e do banco. Nunca confiamos em valores monetários enviados pelo cliente.
 */
public class CheckoutRequest {

    @NotNull(message = "Endereço de entrega é obrigatório")
    private Long addressId;

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }
}
