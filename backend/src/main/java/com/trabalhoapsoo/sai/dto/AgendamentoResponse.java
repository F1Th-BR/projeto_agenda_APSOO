package com.trabalhoapsoo.sai.dto;

import java.time.LocalDateTime;

import com.trabalhoapsoo.sai.model.Agendamento;

/**
 * Agendamento como é mostrado ao(à) profissional (RF007, RF011, RF045).
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record AgendamentoResponse(
    Long id,
    LocalDateTime inicio,
    LocalDateTime fim,
    String nomeCliente,
    String telefoneCliente,
    Long procedimentoId,
    String procedimento,
    String estado,
    String origem,
    LocalDateTime criadoEm,
    String remarcadoPor,
    LocalDateTime remarcadoEm,
    String canceladoPor,
    LocalDateTime canceladoEm,
    String tokenCliente
) {

    /**
     * Converte a entidade no formato enviado pela API.
     * 
     * @param a agendamento
     * @return resposta da API
     */
    public static AgendamentoResponse de(Agendamento a) {
        return new AgendamentoResponse(
            a.getId(), a.getInicioAgendamento(), a.getFimAgendamento(),
            a.getNomeCliente(), a.getTelefoneCliente(),
            a.getProcedimento().getId(), a.getProcedimento().getNomeProcedimento(),
            a.getEstado().name(), a.getOrigem().name(), a.getCriadoEm(),
            a.getRemarcadoPor() == null ? null : a.getRemarcadoPor().name(), a.getRemarcadoEm(),
            a.getCanceladoPor() == null ? null : a.getCanceladoPor().name(), a.getCanceladoEm(),
            a.getTokenCliente());
    }

} // AgendamentoResponse
