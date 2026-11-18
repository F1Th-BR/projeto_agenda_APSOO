package com.trabalhoapsoo.sai.notificacao;

import com.trabalhoapsoo.sai.model.Agendamento;

/**
 * Encaixe para notificações de agendamento (RF041, RF044).
 * 
 * O sistema chama estes métodos sempre que um agendamento muda. Hoje a única
 * implementação apenas registra no log (LogNotificador). Para integrar o
 * WhatsApp no futuro, basta criar outra classe @Component que implemente esta
 * interface (e desativar a do log) — nenhum outro código precisa mudar.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public interface NotificadorAgendamento {

    /**
     * Chamado após um agendamento ser criado.
     * 
     * @param agendamento agendamento criado
     */
    void agendamentoCriado(Agendamento agendamento);

    /**
     * Chamado após um agendamento ser cancelado.
     * 
     * @param agendamento agendamento cancelado
     */
    void agendamentoCancelado(Agendamento agendamento);

    /**
     * Chamado após um agendamento ser remarcado.
     * 
     * @param agendamento agendamento já com o novo horário
     */
    void agendamentoRemarcado(Agendamento agendamento);

} // NotificadorAgendamento
