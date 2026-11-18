package com.trabalhoapsoo.sai.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados para o(a) profissional marcar um atendimento manualmente (RF008).
 * 
 * @param procedimentoId procedimento a ser realizado
 * @param nomeCliente nome da cliente
 * @param telefoneCliente telefone/WhatsApp da cliente
 * @param inicio data e hora de início (horário local, ex.: 2026-11-20T09:00)
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record AgendamentoManualRequest(
    @NotNull(message = "O procedimento é obrigatório.")
    Long procedimentoId,

    @NotBlank(message = "O nome da cliente é obrigatório.")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
    String nomeCliente,

    @NotBlank(message = "O telefone da cliente é obrigatório.")
    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
    String telefoneCliente,

    @NotNull(message = "O horário de início é obrigatório.")
    LocalDateTime inicio
) {
} // AgendamentoManualRequest
