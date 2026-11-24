package com.ecommerce.user;

import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuário não encontrado: " + id
                        )
                );

        return UserResponse.fromEntity(user);
    }

    public UserResponse create(UserCreateRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    "E-mail já cadastrado: " + request.getEmail()
            );
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return UserResponse.fromEntity(savedUser);
    }

    /**
     * Ativa/desativa um usuário. Um usuário inativo não consegue mais fazer login.
     * Atenção: tokens já emitidos continuam válidos até expirar (até 2h), porque
     * a API é stateless e não consulta o banco a cada requisição.
     */
    public UserResponse setActive(Long id, boolean active, Long currentUserId) {
        if (id.equals(currentUserId) && !active) {
            throw new BusinessException("Você não pode desativar a sua própria conta");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuário não encontrado: " + id
                        )
                );

        user.setActive(active);

        return UserResponse.fromEntity(userRepository.save(user));
    }
}