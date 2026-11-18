package com.trabalhoapsoo.sai.repository;

import java.util.List;

import com.trabalhoapsoo.sai.model.Procedimento;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório responsável pelo acesso à tabela 'procedimento'.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public interface ProcedimentoRepository extends JpaRepository<Procedimento, Long> {

    /**
     * Lista os procedimentos ATIVOS de um(a) profissional.
     * Procedimentos removidos (exclusão lógica, RF004) não aparecem.
     * 
     * @param profissionalId id do(a) profissional dono(a) dos procedimentos
     * @return procedimentos ativos do(a) profissional
     */
    List<Procedimento> findByProfissionalIdAndAtivoTrue(Long profissionalId);

} // ProcedimentoRepository
