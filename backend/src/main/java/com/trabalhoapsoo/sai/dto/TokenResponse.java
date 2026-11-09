package com.trabalhoapsoo.sai.dto;

/**
 * Resposta do login: o token JWT que deve ser enviado no cabeçalho
 * 'Authorization: Bearer ...' nas demais requisições.
 * 
 * @param token token JWT assinado
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record TokenResponse(String token) {
} // TokenResponse
