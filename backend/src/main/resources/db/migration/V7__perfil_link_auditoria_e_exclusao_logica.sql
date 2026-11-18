-- ============================================================================
-- PROFISSIONAL: nome, telefone e link público de agendamento (RF027, RF040, RF041)
-- ============================================================================
-- As colunas nascem NULL para não falhar se já houver profissionais cadastrados;
-- depois são preenchidas e só então viram NOT NULL
ALTER TABLE profissional ADD COLUMN nome          VARCHAR(255);
ALTER TABLE profissional ADD COLUMN telefone      VARCHAR(20);
ALTER TABLE profissional ADD COLUMN link_publico  VARCHAR(36);

UPDATE profissional SET nome = split_part(email, '@', 1) WHERE nome IS NULL;
-- Código aleatório (UUID) usado na URL pública. Não é possível adivinhar o link de outro(a) profissional
UPDATE profissional SET link_publico = gen_random_uuid()::text WHERE link_publico IS NULL;

ALTER TABLE profissional ALTER COLUMN nome         SET NOT NULL;
ALTER TABLE profissional ALTER COLUMN link_publico SET NOT NULL;
ALTER TABLE profissional ADD CONSTRAINT uq_profissional_link_publico UNIQUE (link_publico);

-- ============================================================================
-- PROCEDIMENTO: exclusão lógica (RF004)
-- Remover um procedimento apenas o desativa: os agendamentos antigos continuam
-- apontando para ele e permanecem registrados na agenda
-- ============================================================================
ALTER TABLE procedimento ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;

-- ============================================================================
-- AGENDAMENTO: link único da cliente (RF036, RF037) e auditoria (RF045)
-- ============================================================================
ALTER TABLE agendamento ADD COLUMN token_cliente  VARCHAR(36);   -- código do link único da cliente
ALTER TABLE agendamento ADD COLUMN origem         VARCHAR(20);   -- quem criou: CLIENTE, PROFISSIONAL, EMPRESA ou SISTEMA
ALTER TABLE agendamento ADD COLUMN criado_em      TIMESTAMP;
ALTER TABLE agendamento ADD COLUMN remarcado_por  VARCHAR(20);
ALTER TABLE agendamento ADD COLUMN remarcado_em   TIMESTAMP;
ALTER TABLE agendamento ADD COLUMN cancelado_por  VARCHAR(20);
ALTER TABLE agendamento ADD COLUMN cancelado_em   TIMESTAMP;

-- Preenche agendamentos que já existam (dados de teste)
UPDATE agendamento
   SET token_cliente = gen_random_uuid()::text,
       origem        = 'PROFISSIONAL',
       criado_em     = LOCALTIMESTAMP
 WHERE token_cliente IS NULL;

ALTER TABLE agendamento ALTER COLUMN token_cliente SET NOT NULL;
ALTER TABLE agendamento ALTER COLUMN origem        SET NOT NULL;
ALTER TABLE agendamento ALTER COLUMN criado_em     SET NOT NULL;

ALTER TABLE agendamento ADD CONSTRAINT uq_agendamento_token_cliente UNIQUE (token_cliente);
ALTER TABLE agendamento ADD CONSTRAINT ck_agendamento_origem
    CHECK (origem IN ('CLIENTE', 'PROFISSIONAL', 'EMPRESA', 'SISTEMA'));
ALTER TABLE agendamento ADD CONSTRAINT ck_agendamento_remarcado_por
    CHECK (remarcado_por IN ('CLIENTE', 'PROFISSIONAL', 'EMPRESA', 'SISTEMA'));
ALTER TABLE agendamento ADD CONSTRAINT ck_agendamento_cancelado_por
    CHECK (cancelado_por IN ('CLIENTE', 'PROFISSIONAL', 'EMPRESA', 'SISTEMA'));
