-- Um pedido pode ter varias TENTATIVAS de pagamento (ex.: cartao recusado e depois aprovado).
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_reference VARCHAR(100),
    failure_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_payments_amount_positive CHECK (amount > 0),

    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
);

CREATE INDEX idx_payments_order ON payments(order_id);

-- No maximo UM pagamento aprovado por pedido (evita cobranca dupla).
CREATE UNIQUE INDEX uk_payments_one_approved_per_order
    ON payments(order_id)
    WHERE status = 'APPROVED';
