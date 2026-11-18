package com.trabalhoapsoo.sai.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.trabalhoapsoo.sai.model.Agendamento;

/**
 * Agendamento como é mostrado à cliente (sem dados internos).
 * O código 'token' é o do link único usado para consultar, remarcar e cancelar.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record AgendamentoPublicoResponse(
    String token,
    String profissional,
    String procedimento,
    BigDecimal preco,
    LocalDateTime inicio,
    LocalDateTime fim,
    String estado
) {

    /**
     * Converte a entidade no formato enviado à cliente.
     * 
     * @param a agendamento
     * @return resposta pública
     */
    public static AgendamentoPublicoResponse de(Agendamento a) {
        return new AgendamentoPublicoResponse(
            a.getTokenCliente(), a.getProfissional().getNome(),
            a.getProcedimento().getNomeProcedimento(), a.getProcedimento().getPreco(),
            a.getInicioAgendamento(), a.getFimAgendamento(), a.getEstado().name());
    }

} // AgendamentoPublicoResponse
