package com.trabalhoapsoo.sai.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados enviados pela cliente ao agendar pelo link público (RF032–RF035).
 * Informe o 'procedimentoId' (profissional específico) OU a 'chave' do item
 * de uma empresa (qualquer profissional disponível, RF031).
 * Nome e telefone são obrigatórios apenas SEM login; logada, usa os dados da conta (RF034).
 * 
 * @param procedimentoId procedimento de um(a) profissional
 * @param chave chave do item de empresa
 * @param nomeCliente nome da cliente (sem login)
 * @param telefoneCliente telefone/WhatsApp da cliente (sem login)
 * @param inicio horário escolhido (um dos devolvidos em /horarios)
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record AgendamentoPublicoRequest(
    Long procedimentoId,

    String chave,

    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
    String nomeCliente,

    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
    String telefoneCliente,

    @NotNull(message = "O horário é obrigatório.")
    LocalDateTime inicio
) {
} // AgendamentoPublicoRequest
