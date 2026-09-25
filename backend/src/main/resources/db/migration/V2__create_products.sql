CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(150) NOT NULL,
                          description VARCHAR(1000),
                          price NUMERIC(10, 2) NOT NULL,
                          sku VARCHAR(50) NOT NULL,
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          category_id BIGINT NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_products_category
                              FOREIGN KEY (category_id)
                                  REFERENCES categories(id)
);