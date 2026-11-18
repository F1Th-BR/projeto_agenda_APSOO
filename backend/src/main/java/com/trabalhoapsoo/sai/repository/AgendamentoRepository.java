package com.trabalhoapsoo.sai.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.trabalhoapsoo.sai.model.Agendamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositório responsável pelo acesso à tabela 'agendamento'.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

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

    /**
     * Lista os agendamentos ATIVOS (estado AGENDADO) de um(a) profissional
     * que se sobrepõem ao período informado. Usado no cálculo dos horários livres.
     * 
     * @param profissionalId id do(a) profissional
     * @param inicio início do período
     * @param fim fim do período
     * @return agendamentos ativos que tocam o período
     */
    @Query("""
        select a from Agendamento a
        where a.profissional.id = :profissionalId
          and a.estado = com.trabalhoapsoo.sai.model.EstadoAgendamento.AGENDADO
          and a.inicioAgendamento < :fim
          and a.fimAgendamento > :inicio
        """)
    List<Agendamento> buscarAtivosNoPeriodo(@Param("profissionalId") Long profissionalId,
                                            @Param("inicio") LocalDateTime inicio,
                                            @Param("fim") LocalDateTime fim);

    /**
     * Lista todos os agendamentos (inclusive cancelados) de um(a) profissional
     * cujo início cai no período [inicio, fim), em ordem de horário.
     * 
     * @param profissionalId id do(a) profissional
     * @param inicio início do período (inclusive)
     * @param fim fim do período (exclusive)
     * @return agendamentos do período
     */
    @Query("""
        select a from Agendamento a
        where a.profissional.id = :profissionalId
          and a.inicioAgendamento >= :inicio
          and a.inicioAgendamento < :fim
        order by a.inicioAgendamento
        """)
    List<Agendamento> listarNoPeriodo(@Param("profissionalId") Long profissionalId,
                                      @Param("inicio") LocalDateTime inicio,
                                      @Param("fim") LocalDateTime fim);

    /**
     * Lista os agendamentos ATIVOS de um(a) profissional que ainda vão começar.
     * Usado para avisar antes de remover um dia de atendimento (UC002).
     * 
     * @param profissionalId id do(a) profissional
     * @param agora momento de referência
     * @return agendamentos ativos futuros
     */
    @Query("""
        select a from Agendamento a
        where a.profissional.id = :profissionalId
          and a.estado = com.trabalhoapsoo.sai.model.EstadoAgendamento.AGENDADO
          and a.inicioAgendamento >= :agora
        order by a.inicioAgendamento
        """)
    List<Agendamento> listarAtivosAPartirDe(@Param("profissionalId") Long profissionalId,
                                            @Param("agora") LocalDateTime agora);

    /**
     * Lista os agendamentos de uma conta de cliente, do mais recente ao mais antigo (RF026).
     * 
     * @param contaClienteId id da conta
     * @return agendamentos da conta
     */
    List<Agendamento> findByContaClienteIdOrderByInicioAgendamentoDesc(Long contaClienteId);

    /**
     * Lista os agendamentos criados por uma empresa, do mais recente ao mais antigo.
     * 
     * @param empresaId id da empresa
     * @return agendamentos criados pela empresa
     */
    List<Agendamento> findByEmpresaIdOrderByInicioAgendamentoDesc(Long empresaId);

    /**
     * Busca um agendamento pelo código do link único da cliente (RF036, RF037).
     * 
     * @param tokenCliente código do link único
     * @return agendamento encontrado, ou vazio se o código não existir
     */
    Optional<Agendamento> findByTokenCliente(String tokenCliente);

} // AgendamentoRepository
