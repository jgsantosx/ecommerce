package com.ecommerce.stock;

import java.time.LocalDateTime;

public class StockResponse {

    private Long productId;
    private int quantity;
    private LocalDateTime updatedAt;

    public StockResponse() {
    }

    public StockResponse(Long productId, int quantity, LocalDateTime updatedAt) {
        this.productId = productId;
        this.quantity = quantity;
        this.updatedAt = updatedAt;
    }

    public static StockResponse fromEntity(Stock stock) {
        return new StockResponse(
                stock.getProduct().getId(),
                stock.getQuantity(),
                stock.getUpdatedAt()
        );
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
