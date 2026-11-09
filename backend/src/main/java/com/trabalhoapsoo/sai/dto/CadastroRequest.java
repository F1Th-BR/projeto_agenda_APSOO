package com.trabalhoapsoo.sai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados recebidos no cadastro de um(a) profissional.
 * 
 * @param email e-mail, usado como login (RF001)
 * @param senha senha em texto puro; só trafega aqui e é guardada como hash
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record CadastroRequest(
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "O e-mail informado é inválido.")
    String email,

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres.")
    String senha
) {
} // CadastroRequest
