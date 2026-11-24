package com.ecommerce.stock;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.product.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    public void initialize(Product product) {
        stockRepository.save(new Stock(product));
    }

    @Transactional(readOnly = true)
    public StockResponse findByProductId(Long productId) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> notFound(productId));

        return StockResponse.fromEntity(stock);
    }

    @Transactional(readOnly = true)
    public int availableQuantity(Long productId) {
        return stockRepository.findByProductId(productId)
                .map(Stock::getQuantity)
                .orElse(0);
    }

    @Transactional
    public StockResponse increase(Long productId, int quantity) {
        Stock stock = getStockForUpdate(productId);
        stock.increase(quantity);
        return StockResponse.fromEntity(stock);
    }

    /**
     * Saída de estoque. Participa da transação de quem chamar (ex.: criação do
     * pedido): se algo falhar depois, a baixa é desfeita junto (rollback).
     */
    @Transactional
    public StockResponse decrease(Long productId, int quantity) {
        Stock stock = getStockForUpdate(productId);
        stock.decrease(quantity);
        return StockResponse.fromEntity(stock);
    }

    private Stock getStockForUpdate(Long productId) {
        return stockRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> notFound(productId));
    }

    private ResourceNotFoundException notFound(Long productId) {
        return new ResourceNotFoundException(
                "Estoque não encontrado para o produto: " + productId
        );
    }
}
