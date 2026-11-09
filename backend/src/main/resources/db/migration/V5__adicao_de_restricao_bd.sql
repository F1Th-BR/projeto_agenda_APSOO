-- Impede dois agendamentos ATIVOS sobrepostos para o mesmo profissional (RF047, RN04)

-- Extensão necessária para combinar "=" (bigint) com "&&" (intervalos) em um índice GiST
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE agendamento ADD COLUMN profissional_id BIGINT NOT NULL REFERENCES profissional(id);

-- tsrange usa o intervalo [inicio, fim): um atendimento que termina às 10:00 e
-- outro que começa às 10:00 não conflitam.
-- O filtro WHERE ignora agendamentos CANCELADO, liberando o horário (RF049)
ALTER TABLE agendamento ADD CONSTRAINT agendamento_sem_sobreposicao
  EXCLUDE USING gist (
    profissional_id WITH =,
    tsrange(inicio_agendamento, fim_agendamento) WITH &&
  ) WHERE (status = 'AGENDADO');