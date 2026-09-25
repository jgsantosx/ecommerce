package com.ecommerce.product;
import com.ecommerce.category.Category;
import com.ecommerce.category.CategoryRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Eletrônicos");
    }

    @Test
    void shouldCreateProduct() {
        ProductCreateRequest request = new ProductCreateRequest();

        request.setName("Notebook");
        request.setDescription("Notebook para trabalho");
        request.setPrice(new BigDecimal("4500.00"));
        request.setSku("NOTE-001");
        request.setCategoryId(1L);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        Product savedProduct = new Product();
        savedProduct.setName("Notebook");
        savedProduct.setDescription("Notebook para trabalho");
        savedProduct.setPrice(new BigDecimal("4500.00"));
        savedProduct.setSku("NOTE-001");
        savedProduct.setActive(true);
        savedProduct.setCategory(category);

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        ProductResponse result = productService.create(request);

        assertNotNull(result);
        assertEquals("Notebook", result.getName());
        assertEquals(
                new BigDecimal("4500.00"),
                result.getPrice()
        );
        assertEquals("NOTE-001", result.getSku());
        assertTrue(result.isActive());
        assertNotNull(result.getCategory());
        assertEquals("Eletrônicos", result.getCategory().getName());

        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        ProductCreateRequest request = new ProductCreateRequest();

        request.setName("Notebook");
        request.setDescription("Notebook para trabalho");
        request.setPrice(new BigDecimal("4500.00"));
        request.setSku("NOTE-001");
        request.setCategoryId(999L);

        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.create(request)
        );

        assertEquals(
                "Categoria não encontrada: 999",
                exception.getMessage()
        );

        verify(categoryRepository).findById(999L);

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void shouldFindProductById() {
        Product product = new Product();
        product.setName("Notebook");
        product.setCategory(category);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductResponse result = productService.findById(1L);

        assertNotNull(result);
        assertEquals("Notebook", result.getName());
        assertNotNull(result.getCategory());
        assertEquals("Eletrônicos", result.getCategory().getName());

        verify(productRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.findById(999L)
        );

        assertEquals(
                "Produto não encontrado: 999",
                exception.getMessage()
        );

        verify(productRepository).findById(999L);
    }

    @Test
    void shouldUpdateProduct() {
        Product product = new Product();
        product.setName("Notebook antigo");
        product.setPrice(new BigDecimal("4000.00"));
        product.setSku("NOTE-001");
        product.setActive(true);
        product.setCategory(category);

        ProductUpdateRequest request = new ProductUpdateRequest();

        request.setName("Notebook novo");
        request.setDescription("Descrição atualizada");
        request.setPrice(new BigDecimal("4500.00"));
        request.setSku("NOTE-002");
        request.setCategoryId(2L);
        request.setActive(false);

        Category newCategory = new Category();
        newCategory.setName("Informática");

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(categoryRepository.findById(2L))
                .thenReturn(Optional.of(newCategory));

        when(productRepository.save(product))
                .thenReturn(product);

        ProductResponse result = productService.update(1L, request);

        assertNotNull(result);
        assertEquals("Notebook novo", result.getName());
        assertEquals(
                "Descrição atualizada",
                result.getDescription()
        );
        assertEquals(
                new BigDecimal("4500.00"),
                result.getPrice()
        );
        assertEquals("NOTE-002", result.getSku());
        assertFalse(result.isActive());
        assertNotNull(result.getCategory());
        assertEquals("Informática", result.getCategory().getName());

        verify(productRepository).findById(1L);
        verify(categoryRepository).findById(2L);
        verify(productRepository).save(product);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingProduct() {
        ProductUpdateRequest request = new ProductUpdateRequest();

        request.setName("Notebook");
        request.setDescription("Descrição");
        request.setPrice(new BigDecimal("4500.00"));
        request.setSku("NOTE-001");
        request.setCategoryId(1L);
        request.setActive(true);

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.update(999L, request)
        );

        assertEquals(
                "Produto não encontrado: 999",
                exception.getMessage()
        );

        verify(productRepository).findById(999L);

        verify(categoryRepository, never())
                .findById(anyLong());

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithNonExistingCategory() {
        Product product = new Product();
        product.setName("Notebook");

        ProductUpdateRequest request = new ProductUpdateRequest();

        request.setName("Notebook novo");
        request.setDescription("Descrição");
        request.setPrice(new BigDecimal("4500.00"));
        request.setSku("NOTE-002");
        request.setCategoryId(999L);
        request.setActive(true);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.update(1L, request)
        );

        assertEquals(
                "Categoria não encontrada: 999",
                exception.getMessage()
        );

        verify(productRepository).findById(1L);
        verify(categoryRepository).findById(999L);

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void shouldDeleteProduct() {
        Product product = new Product();
        product.setName("Notebook");

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.delete(1L);

        verify(productRepository).findById(1L);
        verify(productRepository).delete(product);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingProduct() {
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.delete(999L)
        );

        assertEquals(
                "Produto não encontrado: 999",
                exception.getMessage()
        );

        verify(productRepository).findById(999L);

        verify(productRepository, never())
                .delete(any(Product.class));
    }
}