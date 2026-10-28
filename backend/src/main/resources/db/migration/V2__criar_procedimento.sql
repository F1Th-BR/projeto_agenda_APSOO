CREATE TABLE procedimento (
    id                   BIGSERIAL PRIMARY KEY,
    nome_procedimento    VARCHAR(255) NOT NULL,
    duracao_procedimento BIGINT NOT NULL,       -- duração em segundos (java.time.Duration)
    preco                NUMERIC(10,2) NOT NULL,
    profissional_id      BIGINT NOT NULL REFERENCES profissional(id)
);