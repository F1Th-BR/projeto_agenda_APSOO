package com.trabalhoapsoo.sai.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

/**
 * Dados recebidos ao cadastrar um intervalo de atendimento (RF005).
 * 
 * @param diaSemana dia da semana, em inglês (MONDAY, TUESDAY, ... SUNDAY)
 * @param horaInicio hora de início, no formato "HH:mm" (ex.: "08:00")
 * @param horaFim hora de término, no formato "HH:mm" (ex.: "12:00")
 * 
 * @author Thiago Feijó de Albuquerque
 */
public record DisponibilidadeRequest(
    @NotNull(message = "O dia da semana é obrigatório.")
    DayOfWeek diaSemana,

    @NotNull(message = "A hora de início é obrigatória.")
    LocalTime horaInicio,

    @NotNull(message = "A hora de término é obrigatória.")
    LocalTime horaFim
) {
} // DisponibilidadeRequest
