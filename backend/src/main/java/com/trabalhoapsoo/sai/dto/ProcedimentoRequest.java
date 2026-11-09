package com.trabalhoapsoo.sai.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Dados recebidos ao cadastrar ou editar um procedimento (RF002, RF003).
 * 
 * @param nome nome do procedimento
 * @param duracaoMinutos duração em minutos (convertida para Duration no service)
 * @param preco preço do procedimento
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record ProcedimentoRequest(
    @NotBlank(message = "O nome do procedimento é obrigatório.")
    String nome,

    @NotNull(message = "A duração é obrigatória.")
    @Positive(message = "A duração deve ser maior que zero (em minutos).")
    Integer duracaoMinutos,

    @NotNull(message = "O preço é obrigatório.")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
    @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 2 casas decimais.")
    BigDecimal preco
) {
} // ProcedimentoRequest
