package com.trabalhoapsoo.sai.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

/**
 * Novo horário informado ao remarcar um agendamento (RF009, RF037).
 * 
 * @param inicio novo horário de início
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record RemarcacaoRequest(
    @NotNull(message = "O novo horário é obrigatório.")
    LocalDateTime inicio
) {
} // RemarcacaoRequest
