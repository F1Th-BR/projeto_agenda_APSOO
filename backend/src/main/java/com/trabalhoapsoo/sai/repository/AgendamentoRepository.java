package com.trabalhoapsoo.sai.repository;

import java.time.LocalDateTime;

import com.trabalhoapsoo.sai.model.Agendamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Conta os agendamentos ATIVOS (estado AGENDADO) de um(a) profissional
     * que se sobrepõem ao período informado. Dois intervalos se sobrepõem
     * quando cada um começa antes de o outro terminar.
     * 
     * @param profissionalId id do(a) profissional
     * @param inicio início do período
     * @param fim fim do período
     * @return quantidade de agendamentos ativos no período
     */
    @Query("""
        select count(a) from Agendamento a
        where a.profissional.id = :profissionalId
          and a.estado = com.trabalhoapsoo.sai.model.EstadoAgendamento.AGENDADO
          and a.inicioAgendamento < :fim
          and a.fimAgendamento > :inicio
        """)
    long contarAtivosNoPeriodo(@Param("profissionalId") Long profissionalId,
                               @Param("inicio") LocalDateTime inicio,
                               @Param("fim") LocalDateTime fim);

} // AgendamentoRepository
