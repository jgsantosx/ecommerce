package com.ecommerce.cart;

import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.product.Product;
import com.ecommerce.product.ProductRepository;
import com.ecommerce.stock.StockService;
import com.ecommerce.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StockService stockService;

    public CartService(
            CartRepository cartRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            StockService stockService
    ) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.stockService = stockService;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .map(CartResponse::fromEntity)
                .orElseGet(CartResponse::empty);
    }

    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest request) {
        Product product = findActiveProduct(request.getProductId());
        Cart cart = getOrCreateCart(userId);

        int currentQuantity = cart.findItem(product.getId())
                .map(CartItem::getQuantity)
                .orElse(0);

        ensureAvailable(product, currentQuantity + request.getQuantity());

        cart.addItem(product, request.getQuantity());
        cartRepository.save(cart);

        return CartResponse.fromEntity(cart);
    }

    @Transactional
    public CartResponse updateItem(Long userId, Long productId, int quantity) {
        Cart cart = findCart(userId);
        Product product = cart.getItem(productId).getProduct();

        ensureAvailable(product, quantity);

        cart.updateItemQuantity(productId, quantity);

        return CartResponse.fromEntity(cart);
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long productId) {
        Cart cart = findCart(userId);
        cart.removeItem(productId);
        return CartResponse.fromEntity(cart);
    }

    @Transactional
    public void clear(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(Cart::clear);
    }

    /**
     * Verificação "suave": avisa cedo que não há estoque, mas NÃO reserva nada.
     * A garantia real (com lock) acontece só na criação do pedido.
     */
    private void ensureAvailable(Product product, int requestedQuantity) {
        int available = stockService.availableQuantity(product.getId());

        if (requestedQuantity > available) {
            throw new BusinessException(
                    "Quantidade indisponível para " + product.getName()
                            + ". Disponível: " + available
            );
        }
    }

    private Product findActiveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produto não encontrado: " + productId
                ));

        if (!product.isActive()) {
            throw new BusinessException("Produto indisponível: " + product.getName());
        }

        return product;
    }

    private Cart findCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrinho vazio"));
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> new Cart(
                        // getReferenceById não faz SELECT: cria um "proxy" só com o id,
                        // suficiente para preencher a chave estrangeira.
                        userRepository.getReferenceById(userId)
                ));
    }
}
