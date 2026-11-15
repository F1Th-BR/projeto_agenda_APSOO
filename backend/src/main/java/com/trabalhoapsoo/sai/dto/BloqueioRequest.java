package com.trabalhoapsoo.sai.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados recebidos ao cadastrar um bloqueio de agenda (RF006).
 * 
 * @param inicio data e hora de início, no formato "yyyy-MM-ddTHH:mm" (ex.: "2026-11-20T08:00")
 * @param fim data e hora de término, no mesmo formato
 * @param motivo motivo do bloqueio (opcional)
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record BloqueioRequest(
    @NotNull(message = "O início do bloqueio é obrigatório.")
    LocalDateTime inicio,

    @NotNull(message = "O fim do bloqueio é obrigatório.")
    LocalDateTime fim,

    @Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres.")
    String motivo
) {
} // BloqueioRequest
