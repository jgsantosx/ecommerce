CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    total NUMERIC(12, 2) NOT NULL,

    -- Copia ("snapshot") do endereco no momento da compra. Se o cliente editar
    -- ou apagar o endereco depois, o pedido continua com o endereco original.
    shipping_recipient_name VARCHAR(150) NOT NULL,
    shipping_zip_code VARCHAR(8) NOT NULL,
    shipping_street VARCHAR(200) NOT NULL,
    shipping_number VARCHAR(20) NOT NULL,
    shipping_complement VARCHAR(100),
    shipping_district VARCHAR(100) NOT NULL,
    shipping_city VARCHAR(100) NOT NULL,
    shipping_state VARCHAR(2) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_orders_total_non_negative CHECK (total >= 0),

    -- Sem ON DELETE CASCADE: pedidos sao historico financeiro, nao somem.
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,

    -- Preco, nome e SKU congelados no momento da compra.
    product_name VARCHAR(150) NOT NULL,
    product_sku VARCHAR(50) NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL,

    CONSTRAINT ck_order_items_quantity_positive CHECK (quantity > 0),

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    -- Sem cascade: um produto que ja foi vendido nao pode ser apagado
    -- (deve ser desativado). O banco recusa e a API responde 409.
    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
);

CREATE INDEX idx_order_items_order ON order_items(order_id);
