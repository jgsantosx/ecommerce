package com.ecommerce.product;

import com.ecommerce.category.CategoryResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldReturnProductWhenIdExists() throws Exception {
        CategoryResponse categoryResponse = new CategoryResponse(
                1L,
                "Informática",
                LocalDateTime.now()
        );

        ProductResponse response = new ProductResponse(
                1L,
                "Notebook",
                "Notebook para trabalho",
                new BigDecimal("4500.00"),
                "NOTE-001",
                true,
                categoryResponse,
                LocalDateTime.now()
        );

        when(productService.findById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Notebook"))
                .andExpect(jsonPath("$.sku")
                        .value("NOTE-001"))
                .andExpect(jsonPath("$.category.name")
                        .value("Informática"));
    }

    @Test
    void shouldReturn404WhenProductDoesNotExist() throws Exception {
        when(productService.findById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Produto não encontrado: 999"
                ));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.message")
                        .value("Produto não encontrado: 999"));
    }

    @Test
    void shouldCreateProduct() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest();

        request.setName("Notebook");
        request.setDescription("Notebook para trabalho");
        request.setPrice(new BigDecimal("4500.00"));
        request.setSku("NOTE-001");
        request.setCategoryId(1L);

        CategoryResponse categoryResponse = new CategoryResponse(
                1L,
                "Informática",
                LocalDateTime.now()
        );

        ProductResponse response = new ProductResponse(
                1L,
                "Notebook",
                "Notebook para trabalho",
                new BigDecimal("4500.00"),
                "NOTE-001",
                true,
                categoryResponse,
                LocalDateTime.now()
        );

        when(productService.create(any(ProductCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Notebook"))
                .andExpect(jsonPath("$.sku")
                        .value("NOTE-001"))
                .andExpect(jsonPath("$.category.name")
                        .value("Informática"));
    }

    @Test
    void shouldReturn400WhenProductNameIsBlank() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest();

        request.setName("");
        request.setPrice(new BigDecimal("4500.00"));
        request.setSku("NOTE-001");
        request.setCategoryId(1L);

        mockMvc.perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.message")
                        .value("Erro de validação"))
                .andExpect(jsonPath("$.errors.name")
                        .value("Nome do produto é obrigatório"));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        ProductUpdateRequest request = new ProductUpdateRequest();

        request.setName("Notebook atualizado");
        request.setDescription("Descrição atualizada");
        request.setPrice(new BigDecimal("5000.00"));
        request.setSku("NOTE-002");
        request.setCategoryId(1L);
        request.setActive(true);

        CategoryResponse categoryResponse = new CategoryResponse(
                1L,
                "Informática",
                LocalDateTime.now()
        );

        ProductResponse response = new ProductResponse(
                1L,
                "Notebook atualizado",
                "Descrição atualizada",
                new BigDecimal("5000.00"),
                "NOTE-002",
                true,
                categoryResponse,
                LocalDateTime.now()
        );

        when(productService.update(
                eq(1L),
                any(ProductUpdateRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/products/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Notebook atualizado"))
                .andExpect(jsonPath("$.sku")
                        .value("NOTE-002"))
                .andExpect(jsonPath("$.category.name")
                        .value("Informática"));
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        doNothing().when(productService).delete(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk());

        verify(productService).delete(1L);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingProduct()
            throws Exception {

        doThrow(new ResourceNotFoundException(
                "Produto não encontrado: 999"
        )).when(productService).delete(999L);

        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.message")
                        .value("Produto não encontrado: 999"));

        verify(productService).delete(999L);
    }
}