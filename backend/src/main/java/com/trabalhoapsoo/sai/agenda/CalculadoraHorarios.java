package com.trabalhoapsoo.sai.agenda;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Classe responsável por calcular os horários livres de um dia (RF005, RF035).
 * 
 * É uma classe "pura": não acessa o banco nem o Spring. Recebe os dados já
 * buscados (janelas de atendimento, agendamentos e bloqueios) e devolve a
 * lista de horários de início possíveis. Por isso é simples de testar.
 * 
 * Regras aplicadas:
 *  - o procedimento inteiro deve caber dentro de uma janela de atendimento;
 *  - o horário não pode sobrepor agendamentos ativos nem bloqueios (RF047);
 *  - o horário de início deve respeitar o mínimo e o máximo permitidos
 *    (antecedência mínima RF050 e janela de 30 dias RF051).
 * 
 * @author Thiago Feijó de Albuquerque
 */
public final class CalculadoraHorarios {
    /* ===== TIPOS AUXILIARES ===== */
    /**
     * Janela de atendimento em um dia (ex.: 08:00 às 12:00).
     * 
     * @param inicio hora de início
     * @param fim hora de fim
     */
    public record JanelaTrabalho(LocalTime inicio, LocalTime fim) {
    } // JanelaTrabalho

    /**
     * Intervalo de data e hora já ocupado (agendamento ou bloqueio).
     * 
     * @param inicio início do intervalo
     * @param fim fim do intervalo
     */
    public record Intervalo(LocalDateTime inicio, LocalDateTime fim) {

        /**
         * Dois intervalos se sobrepõem quando cada um começa antes de o
         * outro terminar. Se um termina exatamente quando o outro começa,
         * NÃO há sobreposição.
         * 
         * @param outroInicio início do outro intervalo
         * @param outroFim fim do outro intervalo
         * @return true se houver sobreposição
         */
        public boolean sobrepoe(LocalDateTime outroInicio, LocalDateTime outroFim) {
            return inicio.isBefore(outroFim) && fim.isAfter(outroInicio);
        }
    } // Intervalo

    /* ===== MÉTODOS ===== */
    private CalculadoraHorarios() {}

    /**
     * Calcula os horários de início livres em um dia.
     * 
     * @param dia dia desejado
     * @param janelas janelas de atendimento daquele dia da semana
     * @param ocupados agendamentos ativos e bloqueios que tocam o dia
     * @param duracao duração do procedimento
     * @param passo intervalo entre horários oferecidos (ex.: 30 minutos)
     * @param inicioMinimo primeiro horário permitido (agora + antecedência mínima)
     * @param inicioMaximo último horário de início permitido (agora + 30 dias)
     * @return horários de início livres, em ordem crescente e sem repetição
     */
    public static List<LocalDateTime> calcular(LocalDate dia,
                                               List<JanelaTrabalho> janelas,
                                               List<Intervalo> ocupados,
                                               Duration duracao,
                                               Duration passo,
                                               LocalDateTime inicioMinimo,
                                               LocalDateTime inicioMaximo) {
        if (duracao.isZero() || duracao.isNegative() || passo.isZero() || passo.isNegative()) {
            throw new IllegalArgumentException("Duração e passo devem ser positivos.");
        }

        TreeSet<LocalDateTime> livres = new TreeSet<>(); // ordena e elimina repetidos (janelas coladas)
        for (JanelaTrabalho janela : janelas) {
            LocalDateTime fimJanela = dia.atTime(janela.fim());
            for (LocalDateTime inicio = dia.atTime(janela.inicio());
                 !inicio.plus(duracao).isAfter(fimJanela);
                 inicio = inicio.plus(passo)) {

                LocalDateTime fim = inicio.plus(duracao);
                if (inicio.isBefore(inicioMinimo) || inicio.isAfter(inicioMaximo)) {
                    continue;
                }
                if (livre(inicio, fim, ocupados)) {
                    livres.add(inicio);
                }
            }
        }
        return new ArrayList<>(livres);
    }

    /**
     * Verifica se um período específico está livre (sem tocar nenhum ocupado).
     * 
     * @param inicio início do período
     * @param fim fim do período
     * @param ocupados intervalos ocupados
     * @return true se o período está livre
     */
    public static boolean livre(LocalDateTime inicio, LocalDateTime fim, List<Intervalo> ocupados) {
        return ocupados.stream().noneMatch(o -> o.sobrepoe(inicio, fim));
    }

} // CalculadoraHorarios
