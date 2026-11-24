CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipient_name VARCHAR(150) NOT NULL,
    zip_code VARCHAR(8) NOT NULL,
    street VARCHAR(200) NOT NULL,
    number VARCHAR(20) NOT NULL,
    complement VARCHAR(100),
    district VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_addresses_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_addresses_user ON addresses(user_id);

-- Indice unico PARCIAL: so considera linhas com is_default = TRUE.
-- Resultado: no maximo um endereco principal por usuario, garantido pelo banco.
CREATE UNIQUE INDEX uk_addresses_one_default_per_user
    ON addresses(user_id)
    WHERE is_default;
