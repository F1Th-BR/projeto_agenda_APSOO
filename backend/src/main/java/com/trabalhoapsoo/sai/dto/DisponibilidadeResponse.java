package com.trabalhoapsoo.sai.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.trabalhoapsoo.sai.model.Disponibilidade;

/**
 * Dados de um intervalo de atendimento devolvidos pela API.
 * 
 * @param id id da disponibilidade
 * @param diaSemana dia da semana
 * @param horaInicio hora de início
 * @param horaFim hora de término
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record DisponibilidadeResponse(Long id, DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFim) {

    /**
     * Converte a entidade no formato devolvido pela API.
     * 
     * @param d disponibilidade a converter
     * @return resposta correspondente
     */
    public static DisponibilidadeResponse de(Disponibilidade d) {
        return new DisponibilidadeResponse(d.getId(), d.getDiaSemana(), d.getHoraInicio(), d.getHoraFim());
    }

} // DisponibilidadeResponse
