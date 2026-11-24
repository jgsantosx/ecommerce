package com.ecommerce.image;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;

@RestController
public class ProductImageController {

    private final ProductImageService imageService;

    public ProductImageController(ProductImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/api/products/{productId}/images")
    public List<ProductImageResponse> findByProduct(@PathVariable Long productId) {
        return imageService.findByProduct(productId);
    }

    /** multipart/form-data com o campo "file". Restrito a ADMIN no SecurityConfig. */
    @PostMapping(
            path = "/api/products/{productId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ProductImageResponse upload(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file
    ) {
        return imageService.upload(productId, file);
    }

    @DeleteMapping("/api/products/{productId}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {
        imageService.delete(productId, imageId);
    }

    /** Entrega o arquivo. Público, com cache no navegador por 1 dia. */
    @GetMapping("/api/images/{imageId}")
    public ResponseEntity<Resource> download(@PathVariable Long imageId) {
        ProductImageService.ImageContent content = imageService.load(imageId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                // Impede o navegador de "adivinhar" outro tipo de conteúdo.
                .header("X-Content-Type-Options", "nosniff")
                .body(content.resource());
    }
}
