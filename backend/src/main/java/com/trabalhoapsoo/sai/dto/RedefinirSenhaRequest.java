package com.trabalhoapsoo.sai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados para definir a nova senha com o código recebido (RF025).
 * 
 * @param email e-mail da conta
 * @param codigo código de verificação recebido
 * @param novaSenha nova senha (mínimo 8 caracteres)
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record RedefinirSenhaRequest(
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "O e-mail informado é inválido.")
    String email,

    @NotBlank(message = "O código é obrigatório.")
    String codigo,

    @NotBlank(message = "A nova senha é obrigatória.")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres.")
    String novaSenha
) {
} // RedefinirSenhaRequest
