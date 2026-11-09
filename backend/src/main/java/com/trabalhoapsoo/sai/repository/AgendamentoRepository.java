package com.trabalhoapsoo.sai.repository;

import com.trabalhoapsoo.sai.model.Agendamento;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório responsável pelo acesso à tabela 'agendamento'.
 * 
 * NOTE: As consultas de disponibilidade (Fase 4) serão adicionadas aqui.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /**
     * Verifica se existe algum agendamento (em qualquer estado) para o
     * procedimento informado. Usado para impedir a remoção de um procedimento
     * que já possui histórico (RF004).
     * 
     * @param procedimentoId id do procedimento
     * @return true se existir ao menos um agendamento
     */
    boolean existsByProcedimentoId(Long procedimentoId);

} // AgendamentoRepository
