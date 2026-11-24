package com.ecommerce.address;

import com.ecommerce.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public List<AddressResponse> findAll(@AuthenticationPrincipal Jwt jwt) {
        return addressService.findAll(AuthenticatedUser.id(jwt));
    }

    @GetMapping("/{id}")
    public AddressResponse findById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        return addressService.findById(AuthenticatedUser.id(jwt), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddressRequest request
    ) {
        return addressService.create(AuthenticatedUser.id(jwt), request);
    }

    @PutMapping("/{id}")
    public AddressResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request
    ) {
        return addressService.update(AuthenticatedUser.id(jwt), id, request);
    }

    @PatchMapping("/{id}/default")
    public AddressResponse setDefault(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        return addressService.setDefault(AuthenticatedUser.id(jwt), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        addressService.delete(AuthenticatedUser.id(jwt), id);
    }
}
