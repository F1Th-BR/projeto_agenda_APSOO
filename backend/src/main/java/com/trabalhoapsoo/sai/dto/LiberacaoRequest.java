package com.trabalhoapsoo.sai.dto;

import jakarta.validation.constraints.Size;

/**
 * Libera um procedimento no link da empresa (RF017). Procedimentos liberados
 * com o mesmo 'nomeNoLink' aparecem agrupados em um único item (RF018).
 * 
 * @param nomeNoLink nome de agrupamento (opcional; vazio usa o nome do procedimento)
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record LiberacaoRequest(
    @Size(max = 255, message = "O nome deve ter no máximo 255 caracteres.")
    String nomeNoLink
) {
} // LiberacaoRequest
