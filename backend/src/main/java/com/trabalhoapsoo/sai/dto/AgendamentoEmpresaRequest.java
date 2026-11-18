package com.trabalhoapsoo.sai.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados para a empresa agendar como se fosse uma cliente (RF021).
 * Informe o 'procedimentoId' (profissional específico) OU a 'chave' do item
 * (qualquer profissional disponível).
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record AgendamentoEmpresaRequest(
    Long procedimentoId,

    String chave,

    @NotBlank(message = "O nome do cliente é obrigatório.")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
    String nomeCliente,

    @NotBlank(message = "O telefone do cliente é obrigatório.")
    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
    String telefoneCliente,

    @NotNull(message = "O horário de início é obrigatório.")
    LocalDateTime inicio
) {
} // AgendamentoEmpresaRequest
