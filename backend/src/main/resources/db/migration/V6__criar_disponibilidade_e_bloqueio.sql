-- Disponibilidade semanal do(a) profissional (RF005): em quais dias da semana
-- e em quais horários ele(a) atende. Um dia pode ter mais de um intervalo
-- (ex.: 08:00-12:00 e 14:00-18:00, deixando o almoço de fora)
CREATE TABLE disponibilidade (
    id              BIGSERIAL PRIMARY KEY,
    profissional_id BIGINT NOT NULL REFERENCES profissional(id),
    dia_semana      VARCHAR(10) NOT NULL,   -- nome do java.time.DayOfWeek (MONDAY, TUESDAY...)
    hora_inicio     TIME NOT NULL,
    hora_fim        TIME NOT NULL,
    CHECK (hora_fim > hora_inicio),
    CHECK (dia_semana IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'))
);

CREATE INDEX idx_disponibilidade_profissional ON disponibilidade (profissional_id);

-- Bloqueios de agenda (RF006): períodos pontuais em que o(a) profissional
-- não atende (folga, consulta médica, feriado...)
CREATE TABLE bloqueio (
    id              BIGSERIAL PRIMARY KEY,
    profissional_id BIGINT NOT NULL REFERENCES profissional(id),
    inicio_bloqueio TIMESTAMP NOT NULL,
    fim_bloqueio    TIMESTAMP NOT NULL,
    motivo          VARCHAR(255),
    CHECK (fim_bloqueio > inicio_bloqueio)
);

CREATE INDEX idx_bloqueio_profissional_inicio ON bloqueio (profissional_id, inicio_bloqueio);

-- Acelera a consulta de horários livres (Fase 4), que busca os agendamentos
-- de um(a) profissional dentro de um período
CREATE INDEX idx_agendamento_profissional_inicio ON agendamento (profissional_id, inicio_agendamento);
