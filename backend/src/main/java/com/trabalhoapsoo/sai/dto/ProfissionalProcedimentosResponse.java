package com.trabalhoapsoo.sai.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Procedimentos de cada profissional vinculado e se estão liberados no link (RF019).
 * 
 * @param profissionalId id do(a) profissional
 * @param profissional nome do(a) profissional
 * @param procedimentos procedimentos ativos dele(a)
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record ProfissionalProcedimentosResponse(Long profissionalId, String profissional,
                                                List<ProcedimentoNoLink> procedimentos) {

    /**
     * Um procedimento e seu estado no link da empresa.
     * 
     * @param procedimentoId id do procedimento
     * @param nome nome do procedimento
     * @param duracaoMinutos duração em minutos
     * @param preco preço
     * @param liberado true se aparece no link
     * @param nomeNoLink nome de agrupamento, se houver
     */
    public record ProcedimentoNoLink(Long procedimentoId, String nome, long duracaoMinutos, BigDecimal preco,
                                     boolean liberado, String nomeNoLink) {
    } // ProcedimentoNoLink

} // ProfissionalProcedimentosResponse
