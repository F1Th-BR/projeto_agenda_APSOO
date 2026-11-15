package com.trabalhoapsoo.sai.dto;

import java.time.LocalDateTime;

import com.trabalhoapsoo.sai.model.Bloqueio;

/**
 * Dados de um bloqueio de agenda devolvidos pela API.
 * 
 * @param id id do bloqueio
 * @param inicio data e hora de início
 * @param fim data e hora de término
 * @param motivo motivo do bloqueio (pode ser nulo)
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record BloqueioResponse(Long id, LocalDateTime inicio, LocalDateTime fim, String motivo) {

    /**
     * Converte a entidade no formato devolvido pela API.
     * 
     * @param b bloqueio a converter
     * @return resposta correspondente
     */
    public static BloqueioResponse de(Bloqueio b) {
        return new BloqueioResponse(b.getId(), b.getInicioBloqueio(), b.getFimBloqueio(), b.getMotivo());
    }

} // BloqueioResponse
