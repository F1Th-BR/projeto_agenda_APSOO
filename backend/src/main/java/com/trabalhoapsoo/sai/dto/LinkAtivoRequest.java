package com.trabalhoapsoo.sai.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Ativa ou desativa o link público da empresa (RF016).
 * 
 * @param ativo true para ativar, false para desativar
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record LinkAtivoRequest(
    @NotNull(message = "Informe se o link deve ficar ativo.")
    Boolean ativo
) {
} // LinkAtivoRequest
