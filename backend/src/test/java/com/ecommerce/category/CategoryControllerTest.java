package com.ecommerce.category;

import com.ecommerce.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.mockito.Mockito.doThrow;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void shouldReturnCategoryWhenIdExists() throws Exception {
        Category category = new Category();
        category.setName("Eletrônicos");

        when(categoryService.findById(1L))
                .thenReturn(category);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Eletrônicos"));
    }

    @Test
    void shouldReturn404WhenCategoryDoesNotExist() throws Exception {
        when(categoryService.findById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Categoria não encontrada: 999"
                ));

        mockMvc.perform(get("/api/categories/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Categoria não encontrada: 999"));
    }

    @Test
    void shouldReturn400WhenCategoryNameIsBlank() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("");

        mockMvc.perform(
                        post("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Erro de validação"))
                .andExpect(jsonPath("$.errors.name")
                        .value("Nome da categoria é obrigatório"));
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("Eletrônicos e Informática");

        Category updatedCategory = new Category();
        updatedCategory.setName("Eletrônicos e Informática");

        when(categoryService.update(
                eq(1L),
                any(CategoryUpdateRequest.class)
        )).thenReturn(updatedCategory);

        mockMvc.perform(
                        put("/api/categories/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Eletrônicos e Informática"));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingCategory() throws Exception {
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("Eletrônicos");

        when(categoryService.update(
                eq(999L),
                any(CategoryUpdateRequest.class)
        )).thenThrow(new ResourceNotFoundException(
                "Categoria não encontrada: 999"
        ));

        mockMvc.perform(
                        put("/api/categories/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Categoria não encontrada: 999"));
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        doNothing().when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk());

        verify(categoryService).delete(1L);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingCategory() throws Exception {
        doThrow(new ResourceNotFoundException(
                "Categoria não encontrada: 999"
        )).when(categoryService).delete(999L);

        mockMvc.perform(delete("/api/categories/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Categoria não encontrada: 999"));

        verify(categoryService).delete(999L);
    }
}