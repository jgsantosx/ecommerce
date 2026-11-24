package com.ecommerce.cart;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.product.Product;
import com.ecommerce.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * cascade = ALL: salvar o carrinho salva os itens junto.
     * orphanRemoval = true: tirar um item da lista apaga a linha no banco.
     * Assim o Cart é a "raiz" que controla seus itens.
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Cart() {
    }

    public Cart(User user) {
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Optional<CartItem> findItem(Long productId) {
        return items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
    }

    public CartItem getItem(Long productId) {
        return findItem(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produto não está no carrinho: " + productId
                ));
    }

    /**
     * Adiciona o produto ou, se ele já estiver no carrinho, soma a quantidade.
     */
    public CartItem addItem(Product product, int quantity) {
        CartItem item = findItem(product.getId())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + quantity);
                    return existing;
                })
                .orElseGet(() -> {
                    CartItem created = new CartItem(this, product, quantity);
                    items.add(created);
                    return created;
                });

        touch();
        return item;
    }

    public void updateItemQuantity(Long productId, int quantity) {
        getItem(productId).setQuantity(quantity);
        touch();
    }

    public void removeItem(Long productId) {
        items.remove(getItem(productId));
        touch();
    }

    public void clear() {
        items.clear();
        touch();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public List<CartItem> getItems() {
        return List.copyOf(items);
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
