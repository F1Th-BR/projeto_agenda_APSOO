CREATE TABLE agendamento (
    id                 BIGSERIAL PRIMARY KEY,
    inicio_agendamento TIMESTAMP NOT NULL,
    fim_agendamento    TIMESTAMP NOT NULL,
    nome_cliente       VARCHAR(255) NOT NULL,
    telefone_cliente   VARCHAR(20) NOT NULL,
    procedimento_id    BIGINT NOT NULL REFERENCES procedimento(id),
    status             VARCHAR(20) NOT NULL,
    CHECK (fim_agendamento > inicio_agendamento)
);