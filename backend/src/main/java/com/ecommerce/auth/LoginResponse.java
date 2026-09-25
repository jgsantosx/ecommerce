package com.ecommerce.auth;

import com.ecommerce.user.UserRole;

public class LoginResponse {

    private String token;
    private Long id;
    private String name;
    private String email;
    private UserRole role;

    public LoginResponse() {
    }

    public LoginResponse(
            String token,
            Long id,
            String name,
            String email,
            UserRole role
    ) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }
}