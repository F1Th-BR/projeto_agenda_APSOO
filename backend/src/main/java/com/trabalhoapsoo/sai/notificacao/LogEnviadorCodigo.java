package com.trabalhoapsoo.sai.notificacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementação provisória: escreve o código no log (apenas para desenvolvimento).
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Component
public class LogEnviadorCodigo implements EnviadorCodigo {
    /* ===== ATRIBUTOS ===== */
    private static final Logger LOG = LoggerFactory.getLogger(LogEnviadorCodigo.class);

    /* ===== MÉTODOS ===== */
    @Override
    public void enviar(String email, String telefone, String canal, String codigo) {
        LOG.info("[CÓDIGO DE REDEFINIÇÃO] canal={} conta={} codigo={}", canal, email, codigo);
    }

} // LogEnviadorCodigo
