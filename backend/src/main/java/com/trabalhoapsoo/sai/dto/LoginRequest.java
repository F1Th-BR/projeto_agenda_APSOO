package com.trabalhoapsoo.sai.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Dados recebidos no login de um(a) profissional (RF001).
 * 
 * @param email e-mail cadastrado
 * @param senha senha em texto puro
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record LoginRequest(
    @NotBlank(message = "O e-mail é obrigatório.")
    String email,

    @NotBlank(message = "A senha é obrigatória.")
    String senha
) {
} // LoginRequest
