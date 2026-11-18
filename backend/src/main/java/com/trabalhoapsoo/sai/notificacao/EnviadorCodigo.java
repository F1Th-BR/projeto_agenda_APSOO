package com.trabalhoapsoo.sai.notificacao;

/**
 * Encaixe para o envio do código de redefinição de senha da cliente (RF025),
 * por e-mail ou SMS. Hoje só escreve no log (LogEnviadorCodigo); para enviar de
 * verdade, crie outra classe @Component que implemente esta interface.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public interface EnviadorCodigo {

    /**
     * Envia o código à cliente.
     * 
     * @param email e-mail da conta
     * @param telefone telefone da conta
     * @param canal "EMAIL" ou "SMS"
     * @param codigo código de verificação em texto puro
     */
    void enviar(String email, String telefone, String canal, String codigo);

} // EnviadorCodigo
