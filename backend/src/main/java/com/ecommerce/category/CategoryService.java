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

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Categoria não encontrada: " + id
                        )
                );
    }

    public Category create(CategoryCreateRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setCreatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }
}