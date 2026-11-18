package com.trabalhoapsoo.sai.model;

/**
 * Estados possíveis do vínculo entre uma empresa e um(a) profissional.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public enum EstadoVinculo {
    PENDENTE,  // convite enviado, aguardando resposta (RF015)
    ATIVO,     // convite aceito (RF013)
    RECUSADO,  // convite recusado (RF013)
    ENCERRADO  // profissional saiu da empresa (RF014, RF053)
} // EstadoVinculo
