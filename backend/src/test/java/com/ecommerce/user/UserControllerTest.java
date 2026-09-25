package com.ecommerce.user;

import com.ecommerce.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturnUserWhenIdExists() throws Exception {
        UserResponse response = new UserResponse(
                1L,
                "João",
                "joao@email.com",
                UserRole.CUSTOMER,
                true,
                LocalDateTime.now()
        );

        when(userService.findById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("João"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist() throws Exception {
        when(userService.findById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Usuário não encontrado: 999"
                ));

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Usuário não encontrado: 999"));
    }

    @Test
    void shouldCreateUser() throws Exception {
        UserCreateRequest request = new UserCreateRequest();

        request.setName("João");
        request.setEmail("joao@email.com");
        request.setPassword("123456");

        UserResponse response = new UserResponse(
                1L,
                "João",
                "joao@email.com",
                UserRole.CUSTOMER,
                true,
                LocalDateTime.now()
        );

        when(userService.create(any(UserCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("João"))
                .andExpect(jsonPath("$.email")
                        .value("joao@email.com"))
                .andExpect(jsonPath("$.role")
                        .value("CUSTOMER"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturn400WhenUserNameIsBlank() throws Exception {
        UserCreateRequest request = new UserCreateRequest();

        request.setName("");
        request.setEmail("joao@email.com");
        request.setPassword("123456");

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Erro de validação"))
                .andExpect(jsonPath("$.errors.name")
                        .value("Nome é obrigatório"));
    }
}