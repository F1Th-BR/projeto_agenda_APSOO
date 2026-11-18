package com.trabalhoapsoo.sai.repository;

import java.util.List;
import java.util.Optional;

import com.trabalhoapsoo.sai.model.ProcedimentoLink;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositório responsável pelo acesso à tabela 'procedimento_link'.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public interface ProcedimentoLinkRepository extends JpaRepository<ProcedimentoLink, Long> {

    /**
     * Busca a liberação de um procedimento no link de uma empresa.
     * 
     * @param empresaId id da empresa
     * @param procedimentoId id do procedimento
     * @return liberação, ou vazio
     */
    Optional<ProcedimentoLink> findByEmpresaIdAndProcedimentoId(Long empresaId, Long procedimentoId);

    /**
     * Lista as liberações de uma empresa (ativas ou não).
     * 
     * @param empresaId id da empresa
     * @return liberações da empresa
     */
    List<ProcedimentoLink> findByEmpresaId(Long empresaId);

    /**
     * Lista as liberações ATIVAS que aparecem no link de uma empresa: o
     * procedimento está ativo e o(a) dono(a) tem vínculo ATIVO com a empresa.
     * 
     * @param empresaId id da empresa
     * @return liberações visíveis para as clientes
     */
    @Query("""
        select l from ProcedimentoLink l
        where l.empresa.id = :empresaId
          and l.ativo = true
          and l.procedimento.ativo = true
          and exists (
              select v.id from VinculoEmpresa v
              where v.empresa.id = :empresaId
                and v.profissional.id = l.procedimento.profissional.id
                and v.status = com.trabalhoapsoo.sai.model.EstadoVinculo.ATIVO)
        """)
    List<ProcedimentoLink> listarVisiveis(@Param("empresaId") Long empresaId);

    /**
     * Lista as liberações ativas de procedimentos de um(a) profissional
     * (usado ao sair da empresa, RF053).
     * 
     * @param profissionalId id do(a) profissional
     * @return liberações ativas dos procedimentos dele(a)
     */
    List<ProcedimentoLink> findByProcedimentoProfissionalIdAndAtivoTrue(Long profissionalId);

    /**
     * Verifica se o procedimento está liberado em algum link de empresa (RF004).
     * 
     * @param procedimentoId id do procedimento
     * @return true se há liberação ativa
     */
    boolean existsByProcedimentoIdAndAtivoTrue(Long procedimentoId);

} // ProcedimentoLinkRepository
