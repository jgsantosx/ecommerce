package com.ecommerce.stock;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByProductId(Long productId);

    /**
     * SELECT ... FOR UPDATE: trava a linha até o fim da transação.
     * Usado em entradas e saídas, para que duas compras simultâneas do último
     * item não consigam ambas passar pela validação.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.product.id = :productId")
    Optional<Stock> findByProductIdForUpdate(@Param("productId") Long productId);
}
