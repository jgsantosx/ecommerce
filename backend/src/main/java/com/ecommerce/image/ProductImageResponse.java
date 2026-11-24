package com.ecommerce.image;

import java.time.LocalDateTime;

/**
 * Expomos uma URL pública (/api/images/{id}), e não a chave interna do storage.
 * Se um dia o storage mudar para S3, a URL pode mudar sem afetar o resto.
 */
public record ProductImageResponse(
        Long id,
        Long productId,
        String url,
        String contentType,
        long sizeBytes,
        LocalDateTime createdAt
) {

    public static ProductImageResponse fromEntity(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getProduct().getId(),
                "/api/images/" + image.getId(),
                image.getContentType(),
                image.getSizeBytes(),
                image.getCreatedAt()
        );
    }
}
