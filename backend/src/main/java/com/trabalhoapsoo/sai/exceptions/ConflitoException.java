package com.trabalhoapsoo.sai.exceptions;

/**
 * Lançada quando a operação conflita com o estado atual dos dados
 * (ex.: e-mail já cadastrado, horário já ocupado). Vira HTTP 409.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public class ConflitoException extends RuntimeException {
    /**
     * Construtor padrão
     */
    public ConflitoException() {
        super("Conflito com o estado atual do recurso.");
    }

    /**
     * Construtor para ConflitoException com mensagem específica
     * 
     * @param mensagem mensagem detalhada explicando o conflito
     */
    public ConflitoException(String mensagem) {
        super(mensagem);
    }

    /**
     * Método toString. Retorna uma representação da exceção
     * no formato de String 
     * 
     * @return representação em String da exceção
     */
    @Override
    public String toString() {
        return "ConflitoException: " + getMessage();
    }

} // ConflitoException
