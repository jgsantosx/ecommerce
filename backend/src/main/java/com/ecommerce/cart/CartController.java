package com.ecommerce.cart;

import com.ecommerce.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Não existe "id do carrinho" na URL: o carrinho é sempre o do usuário do token.
 * Isso impede, por construção, que alguém mexa no carrinho de outra pessoa.
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse get(@AuthenticationPrincipal Jwt jwt) {
        return cartService.getCart(AuthenticatedUser.id(jwt));
    }

    @PostMapping("/items")
    public CartResponse addItem(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CartItemRequest request
    ) {
        return cartService.addItem(AuthenticatedUser.id(jwt), request);
    }

    @PutMapping("/items/{productId}")
    public CartResponse updateItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId,
            @Valid @RequestBody CartItemQuantityRequest request
    ) {
        return cartService.updateItem(
                AuthenticatedUser.id(jwt),
                productId,
                request.getQuantity()
        );
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId
    ) {
        return cartService.removeItem(AuthenticatedUser.id(jwt), productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@AuthenticationPrincipal Jwt jwt) {
        cartService.clear(AuthenticatedUser.id(jwt));
    }
}
