package com.ecommerce.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Long id;
    private Long userId;
    private OrderStatus status;
    private BigDecimal total;
    private ShippingAddressResponse shippingAddress;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderResponse() {
    }

    public static OrderResponse fromEntity(Order order) {
        OrderResponse response = new OrderResponse();
        response.id = order.getId();
        // getId() em um proxy LAZY não dispara consulta: o id já está no proxy.
        response.userId = order.getUser().getId();
        response.status = order.getStatus();
        response.total = order.getTotal();
        response.shippingAddress = ShippingAddressResponse.from(order.getShippingAddress());
        response.items = order.getItems().stream().map(OrderItemResponse::fromEntity).toList();
        response.createdAt = order.getCreatedAt();
        response.updatedAt = order.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public ShippingAddressResponse getShippingAddress() {
        return shippingAddress;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public record OrderItemResponse(
            Long productId,
            String productName,
            String productSku,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal subtotal
    ) {
        static OrderItemResponse fromEntity(OrderItem item) {
            return new OrderItemResponse(
                    item.getProduct().getId(),
                    item.getProductName(),
                    item.getProductSku(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    item.getSubtotal()
            );
        }
    }

    public record ShippingAddressResponse(
            String recipientName,
            String zipCode,
            String street,
            String number,
            String complement,
            String district,
            String city,
            String state
    ) {
        static ShippingAddressResponse from(ShippingAddress address) {
            return new ShippingAddressResponse(
                    address.getRecipientName(),
                    address.getZipCode(),
                    address.getStreet(),
                    address.getNumber(),
                    address.getComplement(),
                    address.getDistrict(),
                    address.getCity(),
                    address.getState()
            );
        }
    }
}
