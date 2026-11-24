package com.ecommerce.stock;

import com.ecommerce.exception.BusinessException;
import com.ecommerce.product.Product;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    /**
     * Controle de concorrência otimista: o Hibernate incrementa esse número a
     * cada UPDATE e falha se outra transação alterou a linha no meio do caminho.
     */
    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Stock() {
    }

    public Stock(Product product) {
        this.product = product;
        this.quantity = 0;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * As regras de entrada e saída ficam na própria entidade: assim é impossível
     * alterar a quantidade sem passar pela validação.
     */
    public void increase(int amount) {
        requirePositive(amount);
        this.quantity += amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void decrease(int amount) {
        requirePositive(amount);

        if (amount > quantity) {
            throw new BusinessException(
                    "Estoque insuficiente para o produto " + product.getName()
                            + ": disponível " + quantity + ", solicitado " + amount
            );
        }

        this.quantity -= amount;
        this.updatedAt = LocalDateTime.now();
    }

    private void requirePositive(int amount) {
        if (amount <= 0) {
            throw new BusinessException("Quantidade deve ser maior que zero");
        }
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
