package com.ecommerce.user;

import com.ecommerce.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldCreateUser() {
        UserCreateRequest request = new UserCreateRequest();

        request.setName("João");
        request.setEmail("joao@email.com");
        request.setPassword("123456");

        User savedUser = new User();

        savedUser.setName("João");
        savedUser.setEmail("joao@email.com");
        savedUser.setPassword("$2a$10$hash");
        savedUser.setRole(UserRole.CUSTOMER);
        savedUser.setActive(true);
        savedUser.setCreatedAt(LocalDateTime.now());

        when(userRepository.existsByEmail("joao@email.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("123456"))
                .thenReturn("$2a$10$hash");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse result = userService.create(request);

        assertEquals("João", result.getName());
        assertEquals("joao@email.com", result.getEmail());
        assertEquals(UserRole.CUSTOMER, result.getRole());
        assertTrue(result.isActive());

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .save(userCaptor.capture());

        User userSentToRepository = userCaptor.getValue();

        assertEquals(
                "$2a$10$hash",
                userSentToRepository.getPassword()
        );

        assertNotEquals(
                "123456",
                userSentToRepository.getPassword()
        );

        verify(userRepository)
                .existsByEmail("joao@email.com");

        verify(passwordEncoder)
                .encode("123456");
    }

    @Test
    void shouldNotCreateUserWhenEmailAlreadyExists() {
        UserCreateRequest request = new UserCreateRequest();

        request.setName("João");
        request.setEmail("joao@email.com");
        request.setPassword("123456");

        when(userRepository.existsByEmail("joao@email.com"))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.create(request)
                );

        assertEquals(
                "E-mail já cadastrado: joao@email.com",
                exception.getMessage()
        );

        verify(userRepository)
                .existsByEmail("joao@email.com");

        verify(passwordEncoder, never())
                .encode(any());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldReturnUserWhenIdExists() {
        User user = new User();

        user.setName("Maria");
        user.setEmail("maria@email.com");
        user.setPassword("$2a$10$hash");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponse result = userService.findById(1L);

        assertEquals("Maria", result.getName());
        assertEquals("maria@email.com", result.getEmail());
        assertEquals(UserRole.CUSTOMER, result.getRole());
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> userService.findById(999L)
                );

        assertEquals(
                "Usuário não encontrado: 999",
                exception.getMessage()
        );
    }

    @Test
    void shouldReturnAllUsers() {
        User user1 = new User();

        user1.setName("João");
        user1.setEmail("joao@email.com");
        user1.setRole(UserRole.CUSTOMER);
        user1.setActive(true);
        user1.setCreatedAt(LocalDateTime.now());

        User user2 = new User();

        user2.setName("Maria");
        user2.setEmail("maria@email.com");
        user2.setRole(UserRole.CUSTOMER);
        user2.setActive(true);
        user2.setCreatedAt(LocalDateTime.now());

        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        List<UserResponse> result = userService.findAll();

        assertEquals(2, result.size());
        assertEquals("João", result.get(0).getName());
        assertEquals("Maria", result.get(1).getName());

        verify(userRepository).findAll();
    }
}