package com.ecommerce.user;

import com.ecommerce.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** Gestão de usuários. Protegido por /api/admin/** no SecurityConfig (ADMIN). */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/{id}/active")
    public UserResponse setActive(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody ActiveRequest request
    ) {
        return userService.setActive(id, request.active(), AuthenticatedUser.id(jwt));
    }

    public record ActiveRequest(
            @NotNull(message = "Campo active é obrigatório") Boolean active
    ) {
    }
}
