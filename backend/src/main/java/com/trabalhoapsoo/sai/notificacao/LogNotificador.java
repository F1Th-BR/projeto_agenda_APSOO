package com.trabalhoapsoo.sai.notificacao;

import com.trabalhoapsoo.sai.model.Agendamento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementação provisória do notificador: só escreve no log.
 * Será substituída pela integração com o WhatsApp.
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Component
public class LogNotificador implements NotificadorAgendamento {
    /* ===== ATRIBUTOS ===== */
    private static final Logger LOG = LoggerFactory.getLogger(LogNotificador.class);

    /* ===== MÉTODOS ===== */
    @Override
    public void agendamentoCriado(Agendamento a) {
        LOG.info("[NOTIFICAÇÃO] Agendamento {} criado para {} em {}", a.getId(), a.getNomeCliente(), a.getInicioAgendamento());
    }

    @Override
    public void agendamentoCancelado(Agendamento a) {
        LOG.info("[NOTIFICAÇÃO] Agendamento {} cancelado por {}", a.getId(), a.getCanceladoPor());
    }

    @Override
    public void agendamentoRemarcado(Agendamento a) {
        LOG.info("[NOTIFICAÇÃO] Agendamento {} remarcado para {}", a.getId(), a.getInicioAgendamento());
    }

} // LogNotificador
