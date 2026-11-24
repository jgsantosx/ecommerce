package com.ecommerce.cart;

import com.ecommerce.exception.BusinessException;
import com.ecommerce.product.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    protected CartItem() {
    }

    CartItem(Cart cart, Product product, int quantity) {
        this.cart = cart;
        this.product = product;
        setQuantity(quantity);
    }

    void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("Quantidade deve ser maior que zero");
        }
        this.quantity = quantity;
    }

    /** Subtotal calculado com o preço ATUAL do produto. */
    public BigDecimal getSubtotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
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
}
