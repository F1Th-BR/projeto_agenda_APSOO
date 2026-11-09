package com.trabalhoapsoo.sai.exceptions;

import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduz as exceções lançadas pelas camadas internas em respostas HTTP
 * com um corpo JSON padronizado ({@link RespostaErro}).
 * 
 * @author Thiago Feijó de Albuquerque
 */
@RestControllerAdvice
public class TratadorGlobalDeExcecoes {
    /* ===== MÉTODOS ===== */
    /**
     * Recurso inexistente (ou que não pertence a quem pediu) -> 404.
     * 
     * @param e exceção lançada
     * @return resposta 404
     */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<RespostaErro> tratarNaoEncontrado(RecursoNaoEncontradoException e) {
        return montar(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /**
     * Conflito com o estado atual dos dados -> 409.
     * 
     * @param e exceção lançada
     * @return resposta 409
     */
    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<RespostaErro> tratarConflito(ConflitoException e) {
        return montar(HttpStatus.CONFLICT, e.getMessage());
    }

    /**
     * Login com e-mail ou senha incorretos -> 401.
     * 
     * @param e exceção lançada
     * @return resposta 401
     */
    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<RespostaErro> tratarCredenciais(CredenciaisInvalidasException e) {
        return montar(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    /**
     * Violação de uma constraint do banco (ex.: e-mail duplicado em duas
     * requisições simultâneas, ou horário sobreposto - V5) -> 409.
     * 
     * @param e exceção lançada
     * @return resposta 409
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespostaErro> tratarIntegridade(DataIntegrityViolationException e) {
        return montar(HttpStatus.CONFLICT, "A operação conflita com dados já existentes.");
    }

    /**
     * Falha na validação (@Valid) do corpo da requisição -> 400.
     * Junta as mensagens de todos os campos inválidos.
     * 
     * @param e exceção lançada
     * @return resposta 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaErro> tratarValidacao(MethodArgumentNotValidException e) {
        String mensagem = e.getBindingResult().getFieldErrors().stream()
            .map(erro -> erro.getDefaultMessage())
            .collect(Collectors.joining(" "));
        return montar(HttpStatus.BAD_REQUEST, mensagem);
    }

    /**
     * Corpo da requisição ausente ou com JSON malformado -> 400.
     * 
     * @param e exceção lançada
     * @return resposta 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespostaErro> tratarCorpoInvalido(HttpMessageNotReadableException e) {
        return montar(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou inválido.");
    }

    /* ===== MÉTODOS PRIVADOS ===== */
    private ResponseEntity<RespostaErro> montar(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new RespostaErro(status.value(), mensagem));
    }

} // TratadorGlobalDeExcecoes
