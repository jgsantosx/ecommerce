package com.ecommerce.security;

import com.ecommerce.user.User;
import com.ecommerce.user.UserRepository;
import com.ecommerce.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAllowPublicUserRegistration() throws Exception {

        User savedUser = new User();

        savedUser.setName("João");
        savedUser.setEmail("joao@email.com");
        savedUser.setPassword("$2a$10$hash");
        savedUser.setRole(UserRole.CUSTOMER);
        savedUser.setActive(true);
        savedUser.setCreatedAt(LocalDateTime.now());

        when(userRepository.existsByEmail("joao@email.com"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        String requestBody = """
                {
                    "name": "João",
                    "email": "joao@email.com",
                    "password": "123456"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowPublicProductCatalog() throws Exception {

        mockMvc.perform(
                        get("/api/products")
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowPublicCategoryCatalog() throws Exception {

        mockMvc.perform(
                        get("/api/categories")
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForProtectedEndpoint() throws Exception {

        mockMvc.perform(
                        get("/api/users")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectCustomerFromAdminEndpoint() throws Exception {

        User user = new User();

        setUserId(user, 1L);

        user.setName("João");
        user.setEmail("joao@email.com");
        user.setPassword(
                passwordEncoder.encode("123456")
        );
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));

        String loginRequest = """
                {
                    "email": "joao@email.com",
                    "password": "123456"
                }
                """;

        String loginResponse = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(loginResponse);

        String token = json.get("token").asText();

        mockMvc.perform(
                        get("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToAccessAdminEndpoint() throws Exception {

        User user = new User();

        setUserId(user, 1L);

        user.setName("Admin");
        user.setEmail("admin@email.com");
        user.setPassword(
                passwordEncoder.encode("123456")
        );
        user.setRole(UserRole.ADMIN);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail("admin@email.com"))
                .thenReturn(Optional.of(user));

        String loginRequest = """
                {
                    "email": "admin@email.com",
                    "password": "123456"
                }
                """;

        String loginResponse = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(loginResponse);

        String token = json.get("token").asText();

        mockMvc.perform(
                        get("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }

    private void setUserId(User user, Long id) throws Exception {

        Field field = User.class.getDeclaredField("id");

        field.setAccessible(true);
        field.set(user, id);
    }
}