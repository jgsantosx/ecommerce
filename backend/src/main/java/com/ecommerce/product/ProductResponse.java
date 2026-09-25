package com.ecommerce.product;

import com.ecommerce.category.CategoryResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String sku;
    private boolean active;
    private CategoryResponse category;
    private LocalDateTime createdAt;

    public ProductResponse() {
    }

    public ProductResponse(
            Long id,
            String name,
            String description,
            BigDecimal price,
            String sku,
            boolean active,
            CategoryResponse category,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.sku = sku;
        this.active = active;
        this.category = category;
        this.createdAt = createdAt;
    }

    public static ProductResponse fromEntity(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSku(),
                product.isActive(),
                CategoryResponse.fromEntity(product.getCategory()),
                product.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getSku() {
        return sku;
    }

    public boolean isActive() {
        return active;
    }

    public CategoryResponse getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}