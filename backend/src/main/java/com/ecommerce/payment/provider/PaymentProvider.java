package com.ecommerce.payment.provider;

/**
 * Contrato que qualquer gateway de pagamento precisa cumprir.
 *
 * O PaymentService conhece SOMENTE esta interface. Para integrar um gateway
 * real, basta criar uma nova classe que implemente PaymentProvider (anotada com
 * @Component) e trocar a propriedade app.payment.provider. Nenhum código de
 * pedido ou pagamento precisa mudar.
 */
public interface PaymentProvider {

    /** Nome usado na configuração (app.payment.provider). */
    String name();

    PaymentResult process(PaymentCharge charge);
}
