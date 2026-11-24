package com.ecommerce;

import com.ecommerce.security.JwtService;
import com.ecommerce.user.User;
import com.ecommerce.user.UserRepository;
import com.ecommerce.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste de integração: Spring inteiro + banco PostgreSQL real (Flyway aplica
 * as migrations). Diferente dos testes de unidade, aqui nada é mock.
 *
 * @Transactional no teste: tudo que for gravado é desfeito (rollback) ao
 * final de cada método, então o banco de desenvolvimento não fica "sujo".
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CheckoutFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String customerToken;
    private String otherCustomerToken;

    @BeforeEach
    void setUp() {
        adminToken = tokenFor(UserRole.ADMIN);
        customerToken = tokenFor(UserRole.CUSTOMER);
        otherCustomerToken = tokenFor(UserRole.CUSTOMER);
    }

    @Test
    void fullPurchaseFlow() throws Exception {
        // Catálogo: só ADMIN escreve
        perform(post("/api/categories"), customerToken, "{\"name\":\"Informática\"}")
                .andExpect(status().isForbidden());

        long categoryId = id(perform(post("/api/categories"), adminToken, "{\"name\":\"Informática\"}")
                .andExpect(status().isOk()));

        long productId = id(perform(post("/api/products"), adminToken, """
                {"name":"Notebook","price":4500.00,"sku":"NB-%s","categoryId":%d}
                """.formatted(UUID.randomUUID().toString().substring(0, 8), categoryId))
                .andExpect(status().isOk()));

        // Produto novo nasce com estoque zero (público para consulta)
        mockMvc.perform(get("/api/products/{id}/stock", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(0));

        // Sem estoque, não entra no carrinho
        perform(post("/api/cart/items"), customerToken, item(productId, 1))
                .andExpect(status().isUnprocessableContent());

        // Entrada de estoque: só ADMIN
        perform(post("/api/products/{id}/stock/increase", productId), customerToken, "{\"quantity\":5}")
                .andExpect(status().isForbidden());
        perform(post("/api/products/{id}/stock/increase", productId), adminToken, "{\"quantity\":5}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));

        // Carrinho
        perform(post("/api/cart/items"), customerToken, item(productId, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.total").value(9000.00));

        // Endereço: o primeiro vira principal automaticamente
        long addressId = id(perform(post("/api/addresses"), customerToken, """
                {"recipientName":"João","zipCode":"01001-000","street":"Praça da Sé",
                 "number":"100","district":"Sé","city":"São Paulo","state":"SP"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.defaultAddress").value(true)));

        // Outro cliente não enxerga o endereço
        perform(get("/api/addresses/{id}", addressId), otherCustomerToken, null)
                .andExpect(status().isNotFound());

        // Checkout
        long orderId = id(perform(post("/api/orders"), customerToken, "{\"addressId\":" + addressId + "}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.total").value(9000.00))
                .andExpect(jsonPath("$.items[0].unitPrice").value(4500.00)));

        mockMvc.perform(get("/api/products/{id}/stock", productId))
                .andExpect(jsonPath("$.quantity").value(3));

        perform(get("/api/cart"), customerToken, null)
                .andExpect(jsonPath("$.totalItems").value(0));

        // Pedido de outro cliente: 404 (não revelamos que existe)
        perform(get("/api/orders/{id}", orderId), otherCustomerToken, null)
                .andExpect(status().isNotFound());

        // Pagamento
        perform(post("/api/orders/{id}/payments", orderId), customerToken, "{\"method\":\"PIX\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.amount").value(9000.00));

        perform(get("/api/orders/{id}", orderId), customerToken, null)
                .andExpect(jsonPath("$.status").value("PAID"));

        // Não paga duas vezes
        perform(post("/api/orders/{id}/payments", orderId), customerToken, "{\"method\":\"PIX\"}")
                .andExpect(status().isUnprocessableContent());

        // Administração do pedido
        perform(patch("/api/admin/orders/{id}/status", orderId), customerToken, "{\"status\":\"SHIPPED\"}")
                .andExpect(status().isForbidden());
        perform(patch("/api/admin/orders/{id}/status", orderId), adminToken, "{\"status\":\"SHIPPED\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
        perform(patch("/api/admin/orders/{id}/status", orderId), adminToken, "{\"status\":\"CANCELED\"}")
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void canceledOrderReturnsStock() throws Exception {
        long productId = createProductWithStock(4);
        long addressId = createAddress(customerToken);

        perform(post("/api/cart/items"), customerToken, item(productId, 3)).andExpect(status().isOk());

        long orderId = id(perform(post("/api/orders"), customerToken, "{\"addressId\":" + addressId + "}")
                .andExpect(status().isCreated()));

        mockMvc.perform(get("/api/products/{id}/stock", productId))
                .andExpect(jsonPath("$.quantity").value(1));

        perform(post("/api/orders/{id}/cancel", orderId), customerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        mockMvc.perform(get("/api/products/{id}/stock", productId))
                .andExpect(jsonPath("$.quantity").value(4));
    }

    @Test
    void imageUploadValidatesRealFileType() throws Exception {
        long productId = createProductWithStock(1);

        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
        MockMultipartFile image = new MockMultipartFile("file", "foto.png", "image/png", png);
        MockMultipartFile fake = new MockMultipartFile("file", "foto.png", "image/png", "<html>".getBytes());

        mockMvc.perform(multipart("/api/products/{id}/images", productId).file(image)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        long imageId = id(mockMvc.perform(multipart("/api/products/{id}/images", productId).file(image)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentType").value("image/png")));

        mockMvc.perform(multipart("/api/products/{id}/images", productId).file(fake)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isUnprocessableContent());

        mockMvc.perform(get("/api/images/{id}", imageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(png));
    }

    @Test
    void meReturnsAuthenticatedUserAndAdminAreaIsProtected() throws Exception {
        perform(get("/api/users/me"), customerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));

        perform(get("/api/admin/orders"), customerToken, null)
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- helpers ----------

    private long createProductWithStock(int quantity) throws Exception {
        long categoryId = id(perform(post("/api/categories"), adminToken, "{\"name\":\"Cat\"}"));
        long productId = id(perform(post("/api/products"), adminToken, """
                {"name":"Mouse","price":100.00,"sku":"MS-%s","categoryId":%d}
                """.formatted(UUID.randomUUID().toString().substring(0, 8), categoryId)));
        perform(post("/api/products/{id}/stock/increase", productId), adminToken,
                "{\"quantity\":" + quantity + "}").andExpect(status().isOk());
        return productId;
    }

    private long createAddress(String token) throws Exception {
        return id(perform(post("/api/addresses"), token, """
                {"recipientName":"Ana","zipCode":"20040020","street":"Rua A",
                 "number":"1","district":"Centro","city":"Rio de Janeiro","state":"rj"}
                """).andExpect(status().isCreated()));
    }

    private String item(long productId, int quantity) {
        return "{\"productId\":" + productId + ",\"quantity\":" + quantity + "}";
    }

    private ResultActions perform(MockHttpServletRequestBuilder request, String token, String body) throws Exception {
        request.header("Authorization", "Bearer " + token);
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mockMvc.perform(request);
    }

    private long id(ResultActions result) throws Exception {
        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        return node.get("id").asLong();
    }

    private String tokenFor(UserRole role) {
        User user = new User();
        user.setName(role.name());
        user.setEmail(role.name().toLowerCase() + "-" + UUID.randomUUID() + "@test.com");
        user.setPassword("$2a$10$naoUsadaNesteTeste");
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        return jwtService.generateToken(userRepository.save(user));
    }
}
