package com.ecommerce.order;

import com.ecommerce.address.Address;
import com.ecommerce.address.AddressService;
import com.ecommerce.cart.Cart;
import com.ecommerce.cart.CartItem;
import com.ecommerce.cart.CartRepository;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.product.Product;
import com.ecommerce.stock.StockService;
import com.ecommerce.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressService addressService;
    private final StockService stockService;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            AddressService addressService,
            StockService stockService,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.addressService = addressService;
        this.stockService = stockService;
        this.userRepository = userRepository;
    }

    /**
     * Transforma o carrinho em pedido.
     *
     * Tudo acontece em UMA transação: baixa de estoque, criação do pedido e
     * limpeza do carrinho. Se qualquer passo falhar (ex.: estoque insuficiente
     * no 3º item), o banco desfaz tudo — inclusive a baixa dos itens 1 e 2.
     */
    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .filter(c -> !c.isEmpty())
                .orElseThrow(() -> new BusinessException("Carrinho vazio"));

        Address address = addressService.getOwned(userId, request.getAddressId());

        Order order = new Order(
                userRepository.getReferenceById(userId),
                ShippingAddress.copyOf(address)
        );

        // Ordenar por produto faz todas as transações travarem as linhas de
        // estoque na mesma ordem, o que evita deadlocks entre compras simultâneas.
        List<CartItem> items = cart.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .toList();

        for (CartItem item : items) {
            Product product = item.getProduct();

            if (!product.isActive()) {
                throw new BusinessException("Produto indisponível: " + product.getName());
            }

            stockService.decrease(product.getId(), item.getQuantity());
            order.addItem(product, item.getQuantity());
        }

        Order saved = orderRepository.save(order);
        cart.clear();

        log.info("Pedido {} criado para o usuário {} (total {})", saved.getId(), userId, saved.getTotal());

        return OrderResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findMine(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findMine(Long userId, Long orderId) {
        return OrderResponse.fromEntity(getOwned(userId, orderId));
    }

    /** O cliente só pode cancelar enquanto o pagamento não foi feito. */
    @Transactional
    public OrderResponse cancelMine(Long userId, Long orderId) {
        Order order = lock(orderId);

        if (!order.belongsTo(userId)) {
            throw notFound(orderId);
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException("Só é possível cancelar pedidos aguardando pagamento");
        }

        cancel(order);
        return OrderResponse.fromEntity(order);
    }

    // ---------- Administração ----------

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll(OrderStatus status) {
        List<Order> orders = status == null
                ? orderRepository.findAllByOrderByCreatedAtDesc()
                : orderRepository.findByStatusOrderByCreatedAtDesc(status);

        return orders.stream().map(OrderResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long orderId) {
        return OrderResponse.fromEntity(
                orderRepository.findById(orderId).orElseThrow(() -> notFound(orderId))
        );
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus next) {
        Order order = lock(orderId);

        if (next == OrderStatus.CANCELED) {
            cancel(order);
        } else {
            order.changeStatus(next);
        }

        log.info("Pedido {} alterado para {}", orderId, next);
        return OrderResponse.fromEntity(order);
    }

    // ---------- Usado pelo módulo de pagamento ----------

    /**
     * Busca o pedido do usuário com lock, para o fluxo de pagamento.
     * Retorna a entidade porque quem chama está no mesmo "domínio" de negócio.
     */
    @Transactional
    public Order lockOwnedForPayment(Long userId, Long orderId) {
        Order order = lock(orderId);

        if (!order.belongsTo(userId)) {
            throw notFound(orderId);
        }

        return order;
    }

    @Transactional
    public void markPaid(Order order) {
        order.changeStatus(OrderStatus.PAID);
        log.info("Pedido {} pago", order.getId());
    }

    // ---------- Internos ----------

    /** Cancelar devolve ao estoque tudo o que o pedido havia baixado. */
    private void cancel(Order order) {
        order.changeStatus(OrderStatus.CANCELED);

        order.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .forEach(item ->
                        stockService.increase(item.getProduct().getId(), item.getQuantity())
                );

        log.info("Pedido {} cancelado; estoque devolvido", order.getId());
    }

    private Order getOwned(Long userId, Long orderId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> notFound(orderId));
    }

    private Order lock(Long orderId) {
        return orderRepository.findWithLockById(orderId)
                .orElseThrow(() -> notFound(orderId));
    }

    private ResourceNotFoundException notFound(Long orderId) {
        return new ResourceNotFoundException("Pedido não encontrado: " + orderId);
    }
}
