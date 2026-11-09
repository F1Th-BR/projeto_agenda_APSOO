package com.trabalhoapsoo.sai.exceptions;

/**
 * Corpo JSON devolvido ao cliente quando ocorre um erro.
 * 
 * @param status código HTTP da resposta (ex.: 404)
 * @param mensagem texto explicando o que aconteceu e como agir (RNF003)
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record RespostaErro(int status, String mensagem) {
} // RespostaErro
