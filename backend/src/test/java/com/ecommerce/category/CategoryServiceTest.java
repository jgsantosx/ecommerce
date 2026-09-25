package com.ecommerce.category;

import com.ecommerce.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Eletrônicos");
    }

    @Test
    void shouldFindCategoryById() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        CategoryResponse result = categoryService.findById(1L);

        assertNotNull(result);
        assertEquals("Eletrônicos", result.getName());

        verify(categoryRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.findById(999L)
        );

        assertEquals(
                "Categoria não encontrada: 999",
                exception.getMessage()
        );

        verify(categoryRepository).findById(999L);
    }
}