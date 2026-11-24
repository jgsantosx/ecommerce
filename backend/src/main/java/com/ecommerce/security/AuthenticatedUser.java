package com.ecommerce.security;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Lê informações do usuário autenticado a partir do JWT validado.
 *
 * O id vem do claim "userId", que nós mesmos colocamos no token (JwtService).
 * Como o token é assinado, o cliente não consegue alterar esse valor.
 */
public final class AuthenticatedUser {

    private AuthenticatedUser() {
    }

    public static Long id(Jwt jwt) {
        Object claim = jwt.getClaim("userId");

        if (claim instanceof Number number) {
            return number.longValue();
        }

        throw new IllegalStateException("Token sem claim userId válido");
    }
}
