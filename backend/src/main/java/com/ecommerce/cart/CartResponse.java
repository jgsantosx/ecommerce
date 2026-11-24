package com.ecommerce.cart;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {

    private List<CartItemResponse> items;
    private int totalItems;
    private BigDecimal total;

    public CartResponse() {
    }

    public CartResponse(List<CartItemResponse> items, int totalItems, BigDecimal total) {
        this.items = items;
        this.totalItems = totalItems;
        this.total = total;
    }

    public static CartResponse empty() {
        return new CartResponse(List.of(), 0, BigDecimal.ZERO);
    }

    public static CartResponse fromEntity(Cart cart) {
        return new CartResponse(
                cart.getItems().stream().map(CartItemResponse::fromEntity).toList(),
                cart.getTotalItems(),
                cart.getTotal()
        );
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public static class CartItemResponse {

        private Long productId;
        private String productName;
        private String sku;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal subtotal;

        public CartItemResponse() {
        }

        public CartItemResponse(
                Long productId,
                String productName,
                String sku,
                BigDecimal unitPrice,
                int quantity,
                BigDecimal subtotal
        ) {
            this.productId = productId;
            this.productName = productName;
            this.sku = sku;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.subtotal = subtotal;
        }

        public static CartItemResponse fromEntity(CartItem item) {
            return new CartItemResponse(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getProduct().getSku(),
                    item.getProduct().getPrice(),
                    item.getQuantity(),
                    item.getSubtotal()
            );
        }

        public Long getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public String getSku() {
            return sku;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }
    }
}
