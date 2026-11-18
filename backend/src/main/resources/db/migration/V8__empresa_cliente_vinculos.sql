-- Configurações do(a) profissional (RF012, RF052)
ALTER TABLE profissional ADD COLUMN antecedencia_lembrete_minutos INTEGER NOT NULL DEFAULT 1440; -- RF012
ALTER TABLE profissional ADD COLUMN prazo_cancelamento_horas      INTEGER NOT NULL DEFAULT 2;    -- RF052
ALTER TABLE profissional ADD CONSTRAINT ck_profissional_prazos
    CHECK (antecedencia_lembrete_minutos >= 0 AND prazo_cancelamento_horas >= 0);

-- Conta da EMPRESA (RF015–RF022): login próprio e link público próprio (RF016)
CREATE TABLE empresa (
    id           BIGSERIAL PRIMARY KEY,
    email        VARCHAR(255) NOT NULL UNIQUE,
    senha_hash   VARCHAR(255) NOT NULL,
    nome         VARCHAR(255) NOT NULL,
    telefone     VARCHAR(20),
    link_publico VARCHAR(36)  NOT NULL UNIQUE,
    link_ativo   BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Conta OPCIONAL da cliente (RF023–RF026). O código de redefinição (RF025)
-- é guardado apenas como hash e expira
CREATE TABLE conta_cliente (
    id                    BIGSERIAL PRIMARY KEY,
    email                 VARCHAR(255) NOT NULL UNIQUE,
    senha_hash            VARCHAR(255) NOT NULL,
    nome_completo         VARCHAR(255) NOT NULL,
    telefone              VARCHAR(20)  NOT NULL,
    codigo_redefinicao_hash VARCHAR(255),
    codigo_expira_em      TIMESTAMP
);

-- Vínculo empresa–profissional (RF013–RF015, RF053, RN08)
CREATE TABLE vinculo_empresa (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES empresa(id),
    profissional_id BIGINT NOT NULL REFERENCES profissional(id),
    status          VARCHAR(10) NOT NULL,
    criado_em       TIMESTAMP NOT NULL,
    atualizado_em   TIMESTAMP NOT NULL,
    CHECK (status IN ('PENDENTE', 'ATIVO', 'RECUSADO', 'ENCERRADO'))
);
-- Só um convite em aberto (ou vínculo ativo) por par empresa/profissional
CREATE UNIQUE INDEX uq_vinculo_aberto ON vinculo_empresa (empresa_id, profissional_id)
    WHERE status IN ('PENDENTE', 'ATIVO');
-- Um(a) profissional só tem um vínculo ATIVO por vez
CREATE UNIQUE INDEX uq_vinculo_ativo_profissional ON vinculo_empresa (profissional_id)
    WHERE status = 'ATIVO';

-- Procedimentos liberados no link da empresa (RF017, RF018, RN02, RN09).
-- 'nome_no_link' agrupa procedimentos duplicados em um único item
CREATE TABLE procedimento_link (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES empresa(id),
    procedimento_id BIGINT NOT NULL REFERENCES procedimento(id),
    nome_no_link    VARCHAR(255),
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (empresa_id, procedimento_id)
);

-- Agendamento: conta da cliente (opcional) e empresa que o criou (opcional)
ALTER TABLE agendamento ADD COLUMN conta_cliente_id BIGINT REFERENCES conta_cliente(id);
ALTER TABLE agendamento ADD COLUMN empresa_id       BIGINT REFERENCES empresa(id);
CREATE INDEX idx_agendamento_conta_cliente ON agendamento (conta_cliente_id);
CREATE INDEX idx_agendamento_empresa ON agendamento (empresa_id);
