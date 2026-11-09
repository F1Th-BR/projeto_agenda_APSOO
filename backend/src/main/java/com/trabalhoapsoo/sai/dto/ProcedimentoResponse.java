package com.trabalhoapsoo.sai.dto;

import java.math.BigDecimal;

import com.trabalhoapsoo.sai.model.Procedimento;

/**
 * Dados de um procedimento devolvidos pela API. Existe para não expor a
 * entidade (e, com ela, o(a) profissional) diretamente.
 * 
 * @param id id do procedimento
 * @param nome nome do procedimento
 * @param duracaoMinutos duração em minutos
 * @param preco preço do procedimento
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record ProcedimentoResponse(Long id, String nome, long duracaoMinutos, BigDecimal preco) {

    /**
     * Converte a entidade no formato devolvido pela API.
     * 
     * @param p procedimento a converter
     * @return resposta correspondente
     */
    public static ProcedimentoResponse de(Procedimento p) {
        return new ProcedimentoResponse(
            p.getId(),
            p.getNomeProcedimento(),
            p.getDuracaoProcedimento().toMinutes(),
            p.getPreco());
    }

} // ProcedimentoResponse
