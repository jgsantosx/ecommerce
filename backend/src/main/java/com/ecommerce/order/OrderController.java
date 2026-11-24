package com.ecommerce.order;

import com.ecommerce.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Pedidos do próprio cliente. */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckoutRequest request
    ) {
        return orderService.checkout(AuthenticatedUser.id(jwt), request);
    }

    @GetMapping
    public List<OrderResponse> findMine(@AuthenticationPrincipal Jwt jwt) {
        return orderService.findMine(AuthenticatedUser.id(jwt));
    }

    @GetMapping("/{id}")
    public OrderResponse findMine(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        return orderService.findMine(AuthenticatedUser.id(jwt), id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        return orderService.cancelMine(AuthenticatedUser.id(jwt), id);
    }
}
