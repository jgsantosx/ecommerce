package com.ecommerce.cart;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * @EntityGraph carrega itens e produtos na mesma consulta (JOIN), evitando
     * o problema "N+1": 1 query para o carrinho + 1 query por item.
     */
    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Cart> findByUserId(Long userId);
}
