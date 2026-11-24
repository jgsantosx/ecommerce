package com.ecommerce.image;

import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.product.Product;
import com.ecommerce.product.ProductRepository;
import com.ecommerce.storage.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
public class ProductImageService {

    static final int MAX_IMAGES_PER_PRODUCT = 10;

    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final StorageService storageService;

    public ProductImageService(
            ProductImageRepository imageRepository,
            ProductRepository productRepository,
            StorageService storageService
    ) {
        this.imageRepository = imageRepository;
        this.productRepository = productRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponse> findByProduct(Long productId) {
        return imageRepository.findByProductIdOrderByIdAsc(productId)
                .stream()
                .map(ProductImageResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ProductImageResponse upload(Long productId, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produto não encontrado: " + productId
                ));

        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo vazio");
        }

        if (imageRepository.countByProductId(productId) >= MAX_IMAGES_PER_PRODUCT) {
            throw new BusinessException(
                    "Limite de " + MAX_IMAGES_PER_PRODUCT + " imagens por produto atingido"
            );
        }

        byte[] content = readBytes(file);

        ImageType type = ImageType.detect(content)
                .orElseThrow(() -> new BusinessException(
                        "Formato não suportado. Envie JPEG, PNG ou WEBP"
                ));

        String key = storageService.store(content, type.getExtension());

        try {
            ProductImage image = imageRepository.save(new ProductImage(
                    product,
                    key,
                    sanitizeFilename(file.getOriginalFilename()),
                    type.getContentType(),
                    content.length
            ));
            return ProductImageResponse.fromEntity(image);
        } catch (RuntimeException e) {
            // Se não conseguimos registrar no banco, não deixamos o arquivo órfão.
            storageService.delete(key);
            throw e;
        }
    }

    @Transactional
    public void delete(Long productId, Long imageId) {
        ProductImage image = imageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> notFound(imageId));

        imageRepository.delete(image);
        imageRepository.flush();

        storageService.delete(image.getStorageKey());
    }

    @Transactional(readOnly = true)
    public ImageContent load(Long imageId) {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> notFound(imageId));

        return new ImageContent(
                storageService.load(image.getStorageKey()),
                image.getContentType()
        );
    }

    public record ImageContent(Resource resource, String contentType) {
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo enviado", e);
        }
    }

    /** O nome original é só informativo; guardamos uma versão limpa e curta. */
    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return null;
        }
        String cleaned = filename.replaceAll("[^A-Za-z0-9._-]", "_");
        return cleaned.length() > 255 ? cleaned.substring(0, 255) : cleaned;
    }

    private ResourceNotFoundException notFound(Long imageId) {
        return new ResourceNotFoundException("Imagem não encontrada: " + imageId);
    }
}
