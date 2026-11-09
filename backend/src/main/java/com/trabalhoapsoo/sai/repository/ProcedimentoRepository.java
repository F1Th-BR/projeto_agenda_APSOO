package com.trabalhoapsoo.sai.repository;

import com.trabalhoapsoo.sai.model.Procedimento;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Repositório responsável pelo acesso à tabela 'procedimento'.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public interface ProcedimentoRepository extends JpaRepository<Procedimento, Long> {
    /**
     * Lista os procedimentos de um(a) profissional.
     * 
     * @param profissionalId id do(a) profissional dono(a) dos procedimentos
     * @return procedimentos do(a) profissional
     */
    List<Procedimento> findByProfissionalId(Long profissionalId);
} // ProcedimentoRepository