package com.trabalhoapsoo.sai.exceptions;

/**
 * Lançada quando um recurso buscado por id não existe no banco
 */
public class RecursoNaoEncontradoException extends RuntimeException {
    /**
     * Construtor padrão
     */
    public RecursoNaoEncontradoException() {
        super("Recurso não encontrado.");
    }

    /**
     * Construtor para RecursoNaoEncontradoException com mensagem específica
     * 
     * @param mensagem mensagem detalhada explicando a exceção
     */
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    /**
     * Construtor para RecursoNaoEncontradoException com causa específica
     * 
     * @param causa a causa da exceção
     */
    public RecursoNaoEncontradoException(Throwable causa) {
        super(causa);
    }

    /**
     * Construtor para RecursoNaoEncontradoException com mensagem específica e causa
     * 
     * @param mensagem a mensagem detalhada da exceção
     * @param causa a causa da exceção
     */
    public RecursoNaoEncontradoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    /**
     * Método toString. Retorna uma representação da exceção
     * no formato de String 
     * 
     * @return representação em String da exceção
     */
    @Override
    public String toString() {
        return "RecursoNaoEncontradoException: " + getMessage();
    }

} // RecursoNaoEncontradoException
