package com.ecommerce.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PasswordEncoderTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldEncodePassword() {
        String rawPassword = "123456";

        String encodedPassword =
                passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encodedPassword.startsWith("$2"));
    }

    @Test
    void shouldMatchCorrectPassword() {
        String rawPassword = "123456";

        String encodedPassword =
                passwordEncoder.encode(rawPassword);

        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        encodedPassword
                )
        );
    }

    @Test
    void shouldNotMatchIncorrectPassword() {
        String rawPassword = "123456";

        String encodedPassword =
                passwordEncoder.encode(rawPassword);

        assertFalse(
                passwordEncoder.matches(
                        "senha-errada",
                        encodedPassword
                )
        );
    }

    @Test
    void shouldGenerateDifferentHashesForSamePassword() {
        String rawPassword = "123456";

        String firstHash =
                passwordEncoder.encode(rawPassword);

        String secondHash =
                passwordEncoder.encode(rawPassword);

        assertNotEquals(firstHash, secondHash);

        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        firstHash
                )
        );

        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        secondHash
                )
        );
    }
}