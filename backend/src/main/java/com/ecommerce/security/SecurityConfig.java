package com.ecommerce.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                // Usa o bean CorsConfigurationSource abaixo (frontend em outra origem).
                .cors(Customizer.withDefaults())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                (request, response, authException) ->
                                        response.sendError(
                                                401,
                                                "Unauthorized"
                                        )
                        )
                )

                // As regras são avaliadas de cima para baixo: a primeira que
                // casar com a requisição decide. Por isso as mais específicas vêm antes.
                .authorizeHttpRequests(auth -> auth

                        // ---- Público ----
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categories",
                                "/api/categories/**",
                                "/api/products",
                                "/api/products/**",
                                "/api/images/**"
                        ).permitAll()

                        // ---- Catálogo: escrita só para ADMIN ----
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/categories", "/api/categories/**",
                                "/api/products", "/api/products/**"
                        ).hasRole(ADMIN)
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/categories/**", "/api/products/**"
                        ).hasRole(ADMIN)
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/categories/**", "/api/products/**"
                        ).hasRole(ADMIN)

                        // ---- Usuários ----
                        // /me precisa vir antes da regra de ADMIN para /api/users/**.
                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users",
                                "/api/users/**"
                        ).hasRole(ADMIN)

                        // ---- Área administrativa ----
                        .requestMatchers("/api/admin/**").hasRole(ADMIN)

                        // Carrinho, endereços, pedidos, pagamentos...
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }

    /**
     * CORS: o navegador bloqueia chamadas de uma origem (ex.: http://localhost:5173,
     * o frontend) para outra (http://localhost:8080, a API), a menos que a API
     * autorize explicitamente essa origem.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") List<String> allowedOrigins
    ) {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
