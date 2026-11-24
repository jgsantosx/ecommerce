package com.ecommerce.stock;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/{productId}/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public StockResponse find(@PathVariable Long productId) {
        return stockService.findByProductId(productId);
    }

    @PostMapping("/increase")
    public StockResponse increase(
            @PathVariable Long productId,
            @Valid @RequestBody StockMovementRequest request
    ) {
        return stockService.increase(productId, request.getQuantity());
    }

    @PostMapping("/decrease")
    public StockResponse decrease(
            @PathVariable Long productId,
            @Valid @RequestBody StockMovementRequest request
    ) {
        return stockService.decrease(productId, request.getQuantity());
    }
}
