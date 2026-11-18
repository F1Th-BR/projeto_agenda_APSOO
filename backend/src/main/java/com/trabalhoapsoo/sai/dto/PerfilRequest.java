package com.trabalhoapsoo.sai.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados para atualizar o perfil do(a) profissional.
 * 
 * @param nome novo nome
 * @param telefone novo telefone (opcional)
 * @param antecedenciaLembreteMinutos antecedência do lembrete (opcional, RF012)
 * @param prazoCancelamentoHoras prazo mínimo para cliente/empresa cancelar ou remarcar (opcional, RF052)
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record PerfilRequest(
    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
    String nome,

    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
    String telefone,

    @Min(value = 0, message = "A antecedência do lembrete não pode ser negativa.")
    Integer antecedenciaLembreteMinutos,

    @Min(value = 0, message = "O prazo de cancelamento não pode ser negativo.")
    Integer prazoCancelamentoHoras
) {
} // PerfilRequest
