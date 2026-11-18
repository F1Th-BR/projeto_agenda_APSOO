package com.trabalhoapsoo.sai.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.trabalhoapsoo.sai.model.Bloqueio;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório responsável pelo acesso à tabela 'bloqueio'.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public interface BloqueioRepository extends JpaRepository<Bloqueio, Long> {

    /**
     * Lista os bloqueios de um(a) profissional que ainda não terminaram,
     * do mais próximo para o mais distante.
     * 
     * @param profissionalId id do(a) profissional
     * @param agora momento de referência; bloqueios que terminam antes dele são ignorados
     * @return bloqueios em andamento ou futuros
     */
    List<Bloqueio> findByProfissionalIdAndFimBloqueioAfterOrderByInicioBloqueioAsc(Long profissionalId, LocalDateTime agora);

    /**
     * Lista os bloqueios de um(a) profissional que se sobrepõem a um período.
     * Um bloqueio se sobrepõe quando termina depois do início do período
     * e começa antes do fim dele.
     * 
     * @param profissionalId id do(a) profissional
     * @param inicioPeriodo início do período
     * @param fimPeriodo fim do período
     * @return bloqueios que tocam o período
     */
    List<Bloqueio> findByProfissionalIdAndFimBloqueioAfterAndInicioBloqueioBefore(Long profissionalId,
                                                                                   LocalDateTime inicioPeriodo,
                                                                                   LocalDateTime fimPeriodo);

} // BloqueioRepository
