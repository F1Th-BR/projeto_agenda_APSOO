package com.trabalhoapsoo.sai.model;

/**
 * Enum para identificar quem realizou uma ação sobre um agendamento
 * (criar, remarcar ou cancelar). Usado na auditoria (RF045).
 * 
 * @author Thiago Feijó de Albuquerque 
 */
public enum AtorAgendamento {
    CLIENTE,
    PROFISSIONAL,
    EMPRESA,
    SISTEMA // ações automáticas (ex.: cancelamento por falta de confirmação)
} // AtorAgendamento
