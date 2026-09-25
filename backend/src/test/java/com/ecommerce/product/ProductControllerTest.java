package com.ecommerce.product;

import com.ecommerce.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

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
        Product product = new Product();

        product.setName("Notebook");
        product.setPrice(new BigDecimal("4500.00"));
        product.setSku("NOTE-001");

        when(productService.findById(1L))
                .thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Notebook"))
                .andExpect(jsonPath("$.sku")
                        .value("NOTE-001"));
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

        Product product = new Product();

        product.setName("Notebook");
        product.setDescription("Notebook para trabalho");
        product.setPrice(new BigDecimal("4500.00"));
        product.setSku("NOTE-001");
        product.setActive(true);

        when(productService.create(any(ProductCreateRequest.class)))
                .thenReturn(product);

        mockMvc.perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Notebook"))
                .andExpect(jsonPath("$.sku")
                        .value("NOTE-001"));
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

        Product product = new Product();

        product.setName("Notebook atualizado");
        product.setPrice(new BigDecimal("5000.00"));
        product.setSku("NOTE-002");
        product.setActive(true);

        when(productService.update(
                eq(1L),
                any(ProductUpdateRequest.class)
        )).thenReturn(product);

        mockMvc.perform(
                        put("/api/products/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Notebook atualizado"))
                .andExpect(jsonPath("$.sku")
                        .value("NOTE-002"));
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
