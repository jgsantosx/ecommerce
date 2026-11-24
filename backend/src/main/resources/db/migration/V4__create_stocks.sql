-- Estoque fica em tabela propria (1 linha por produto), separado do catalogo.
CREATE TABLE stocks (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_stocks_product UNIQUE (product_id),

    -- Ultima linha de defesa: o banco recusa estoque negativo mesmo se o codigo falhar.
    CONSTRAINT ck_stocks_quantity_non_negative CHECK (quantity >= 0),

    -- O estoque so existe junto com o produto.
    CONSTRAINT fk_stocks_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);

-- Produtos que ja existiam ganham um registro de estoque zerado.
INSERT INTO stocks (product_id, quantity)
SELECT id, 0 FROM products;
