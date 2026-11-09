package com.trabalhoapsoo.sai.exceptions;

/**
 * Lançada quando o e-mail ou a senha informados no login estão incorretos.
 * A mensagem é a mesma nos dois casos, para não revelar se o e-mail existe.
 * Vira HTTP 401.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public class CredenciaisInvalidasException extends RuntimeException {
    /**
     * Construtor padrão
     */
    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos.");
    }

    /**
     * Método toString. Retorna uma representação da exceção
     * no formato de String 
     * 
     * @return representação em String da exceção
     */
    @Override
    public String toString() {
        return "CredenciaisInvalidasException: " + getMessage();
    }

} // CredenciaisInvalidasException
