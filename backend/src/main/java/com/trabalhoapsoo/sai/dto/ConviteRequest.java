package com.trabalhoapsoo.sai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Convite de uma empresa a um(a) profissional já cadastrado(a) (RF015).
 * 
 * @param emailProfissional e-mail da conta do(a) profissional
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record ConviteRequest(
    @NotBlank(message = "O e-mail do(a) profissional é obrigatório.")
    @Email(message = "O e-mail informado é inválido.")
    String emailProfissional
) {
} // ConviteRequest
