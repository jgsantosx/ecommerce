package com.ecommerce.payment.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Escolhe o provider ativo pela configuração.
 *
 * O Spring injeta em "providers" TODOS os beans que implementam PaymentProvider.
 * Se o nome configurado não existir, a aplicação falha ao iniciar (fail fast),
 * em vez de falhar só no primeiro pagamento de um cliente.
 */
@Component
public class PaymentProviderResolver {

    private final PaymentProvider active;

    public PaymentProviderResolver(
            List<PaymentProvider> providers,
            @Value("${app.payment.provider}") String activeName
    ) {
        Map<String, PaymentProvider> byName = providers.stream()
                .collect(Collectors.toMap(PaymentProvider::name, Function.identity()));

        this.active = byName.get(activeName);

        if (active == null) {
            throw new IllegalStateException(
                    "Provider de pagamento desconhecido: " + activeName
                            + ". Disponíveis: " + byName.keySet()
            );
        }
    }

    public PaymentProvider active() {
        return active;
    }
}
