package com.trabalhoapsoo.sai.exceptions;

/**
 * Lançada quando os dados enviados violam uma regra de negócio que a
 * validação simples dos campos não cobre (ex.: horário de fim antes do
 * horário de início). Vira HTTP 400.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public class RequisicaoInvalidaException extends RuntimeException {
    /**
     * Construtor para RequisicaoInvalidaException com mensagem específica
     * 
     * @param mensagem mensagem detalhada explicando o que está inválido
     */
    public RequisicaoInvalidaException(String mensagem) {
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
        return "RequisicaoInvalidaException: " + getMessage();
    }

} // RequisicaoInvalidaException
