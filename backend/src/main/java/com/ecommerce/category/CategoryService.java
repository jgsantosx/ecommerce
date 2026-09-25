package com.ecommerce.category;

import com.ecommerce.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    public CategoryResponse findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Categoria não encontrada: " + id
                        )
                );

        return CategoryResponse.fromEntity(category);
    }

    public CategoryResponse create(CategoryCreateRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setCreatedAt(LocalDateTime.now());

        Category savedCategory = categoryRepository.save(category);

        return CategoryResponse.fromEntity(savedCategory);
    }

    public CategoryResponse update(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Categoria não encontrada: " + id
                        )
                );

        category.setName(request.getName());

        Category updatedCategory = categoryRepository.save(category);

        return CategoryResponse.fromEntity(updatedCategory);
    }

    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Categoria não encontrada: " + id
                        )
                );

        categoryRepository.delete(category);
    }
}