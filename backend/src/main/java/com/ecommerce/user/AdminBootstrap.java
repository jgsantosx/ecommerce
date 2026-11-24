package com.ecommerce.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Cria o primeiro administrador ao iniciar a aplicação.
 *
 * Por que não um INSERT em migration? Porque a senha (mesmo em hash) ficaria
 * versionada no Git e seria igual em todos os ambientes. Aqui ela vem de
 * variáveis de ambiente (ADMIN_EMAIL / ADMIN_PASSWORD) e só é usada se o
 * e-mail ainda não existir. Sem as variáveis, nada acontece.
 *
 * ApplicationRunner: o Spring executa run() uma vez, logo após a aplicação subir.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }

        if (password.length() < 8) {
            log.warn("ADMIN_PASSWORD deve ter pelo menos 8 caracteres; administrador não criado");
            return;
        }

        if (userRepository.existsByEmail(email)) {
            return;
        }

        User admin = new User();
        admin.setName("Administrador");
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        admin.setCreatedAt(LocalDateTime.now());

        userRepository.save(admin);

        log.info("Administrador inicial criado: {}", email);
    }
}
