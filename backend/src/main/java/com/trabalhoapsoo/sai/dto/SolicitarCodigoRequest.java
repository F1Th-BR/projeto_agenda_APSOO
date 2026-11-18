package com.trabalhoapsoo.sai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Pedido de código para redefinir a senha da cliente (RF025).
 * 
 * @param email e-mail da conta
 * @param canal "EMAIL" ou "SMS" (padrão: EMAIL)
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record SolicitarCodigoRequest(
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "O e-mail informado é inválido.")
    String email,

    String canal
) {
} // SolicitarCodigoRequest
