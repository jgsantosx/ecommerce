-- Cada usuario tem no maximo um carrinho ativo.
CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_carts_user UNIQUE (user_id),

    CONSTRAINT fk_carts_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- O carrinho NAO guarda preco: ele sempre mostra o preco atual do produto.
-- O preco so e "congelado" quando o pedido e criado (order_items).
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,

    CONSTRAINT ck_cart_items_quantity_positive CHECK (quantity > 0),

    -- Um produto aparece uma unica vez por carrinho (a quantidade e somada).
    CONSTRAINT uk_cart_items_cart_product UNIQUE (cart_id, product_id),

    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id)
        REFERENCES carts(id)
        ON DELETE CASCADE,

    -- Se o produto for removido do catalogo, ele sai dos carrinhos.
    CONSTRAINT fk_cart_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);
