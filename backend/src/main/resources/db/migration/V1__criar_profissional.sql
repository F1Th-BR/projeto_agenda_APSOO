CREATE TABLE profissional (
    id          BIGSERIAL PRIMARY KEY,
    usuario     VARCHAR(255) NOT NULL UNIQUE,
    senha_hash  VARCHAR(255) NOT NULL
);