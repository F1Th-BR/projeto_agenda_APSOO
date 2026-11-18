package com.trabalhoapsoo.sai.repository;

import java.util.List;
import java.util.Optional;

import com.trabalhoapsoo.sai.model.EstadoVinculo;
import com.trabalhoapsoo.sai.model.VinculoEmpresa;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório responsável pelo acesso à tabela 'vinculo_empresa'.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public interface VinculoEmpresaRepository extends JpaRepository<VinculoEmpresa, Long> {

    /**
     * Lista os vínculos de uma empresa em um determinado estado.
     * 
     * @param empresaId id da empresa
     * @param status estado desejado
     * @return vínculos encontrados
     */
    List<VinculoEmpresa> findByEmpresaIdAndStatus(Long empresaId, EstadoVinculo status);

    /**
     * Lista os vínculos de um(a) profissional em um determinado estado.
     * 
     * @param profissionalId id do(a) profissional
     * @param status estado desejado
     * @return vínculos encontrados
     */
    List<VinculoEmpresa> findByProfissionalIdAndStatus(Long profissionalId, EstadoVinculo status);

    /**
     * Busca o vínculo ATIVO de um(a) profissional.
     * 
     * @param profissionalId id do(a) profissional
     * @param status estado (use ATIVO)
     * @return vínculo, ou vazio
     */
    Optional<VinculoEmpresa> findFirstByProfissionalIdAndStatus(Long profissionalId, EstadoVinculo status);

    /**
     * Verifica se existe vínculo entre empresa e profissional em um estado.
     * 
     * @param empresaId id da empresa
     * @param profissionalId id do(a) profissional
     * @param status estado
     * @return true se existe
     */
    boolean existsByEmpresaIdAndProfissionalIdAndStatus(Long empresaId, Long profissionalId, EstadoVinculo status);

} // VinculoEmpresaRepository
