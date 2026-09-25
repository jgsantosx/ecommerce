package com.ecommerce.product;

import com.ecommerce.category.Category;
import com.ecommerce.category.CategoryRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Produto não encontrado: " + id
                        )
                );

        return ProductResponse.fromEntity(product);
    }

    public ProductResponse create(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Categoria não encontrada: " + request.getCategoryId()
                        )
                );

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSku(request.getSku());
        product.setActive(true);
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        return ProductResponse.fromEntity(savedProduct);
    }

    public ProductResponse update(
            Long id,
            ProductUpdateRequest request
    ) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Produto não encontrado: " + id
                        )
                );

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Categoria não encontrada: " + request.getCategoryId()
                        )
                );

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSku(request.getSku());
        product.setCategory(category);

        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        Product updatedProduct = productRepository.save(product);

        return ProductResponse.fromEntity(updatedProduct);
    }

    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Produto não encontrado: " + id
                        )
                );

        productRepository.delete(product);
    }
}