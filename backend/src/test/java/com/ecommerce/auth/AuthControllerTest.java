package com.ecommerce.auth;

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

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldLoginWithValidCredentials() throws Exception {

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

        String requestBody = """
                {
                    "email": "joao@email.com",
                    "password": "123456"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.name").value("João"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void shouldRejectLoginWithInvalidPassword() throws Exception {

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

        String requestBody = """
                {
                    "email": "joao@email.com",
                    "password": "senha-errada"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLoginWhenUserDoesNotExist() throws Exception {

        when(userRepository.findByEmail("naoexiste@email.com"))
                .thenReturn(Optional.empty());

        String requestBody = """
                {
                    "email": "naoexiste@email.com",
                    "password": "123456"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isUnauthorized());
    }

    private void setUserId(User user, Long id) throws Exception {

        Field field = User.class.getDeclaredField("id");

        field.setAccessible(true);
        field.set(user, id);
    }
}