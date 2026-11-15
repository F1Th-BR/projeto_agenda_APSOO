package com.trabalhoapsoo.sai.repository;

import java.time.DayOfWeek;
import java.util.List;

import com.trabalhoapsoo.sai.model.Disponibilidade;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório responsável pelo acesso à tabela 'disponibilidade'.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public interface DisponibilidadeRepository extends JpaRepository<Disponibilidade, Long> {

    /**
     * Lista todos os intervalos de atendimento de um(a) profissional.
     * 
     * @param profissionalId id do(a) profissional
     * @return intervalos de atendimento (sem ordem garantida)
     */
    List<Disponibilidade> findByProfissionalId(Long profissionalId);

    /**
     * Lista os intervalos de um(a) profissional em um dia da semana.
     * Usado para impedir intervalos sobrepostos no mesmo dia.
     * 
     * @param profissionalId id do(a) profissional
     * @param diaSemana dia da semana
     * @return intervalos daquele dia
     */
    List<Disponibilidade> findByProfissionalIdAndDiaSemana(Long profissionalId, DayOfWeek diaSemana);

} // DisponibilidadeRepository
