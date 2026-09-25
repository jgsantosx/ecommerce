package com.ecommerce.category;

import java.time.LocalDateTime;

public class CategoryResponse {

    private Long id;
    private String name;
    private LocalDateTime createdAt;

    public CategoryResponse() {
    }

    public CategoryResponse(
            Long id,
            String name,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}