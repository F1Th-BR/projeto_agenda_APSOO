package com.trabalhoapsoo.sai.agenda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.trabalhoapsoo.sai.agenda.CalculadoraHorarios.Intervalo;
import com.trabalhoapsoo.sai.agenda.CalculadoraHorarios.JanelaTrabalho;

import org.junit.jupiter.api.Test;

/**
 * Testes da regra de cálculo de horários livres.
 * 
 * @author Thiago Feijó de Albuquerque
 */
class CalculadoraHorariosTest {
    private static final LocalDate DIA = LocalDate.of(2026, 11, 20);
    private static final Duration PASSO = Duration.ofMinutes(30);
    private static final LocalDateTime SEM_MINIMO = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime SEM_MAXIMO = LocalDateTime.of(2027, 1, 1, 0, 0);

    private static LocalDateTime h(int hora, int minuto) {
        return DIA.atTime(hora, minuto);
    }

    private static JanelaTrabalho janela(int h1, int h2) {
        return new JanelaTrabalho(LocalTime.of(h1, 0), LocalTime.of(h2, 0));
    }

    @Test
    void geraHorariosAteOUltimoQueCabe() {
        // Janela 08-10, procedimento de 60 min: 08:00, 08:30 e 09:00 (09:30 estouraria a janela)
        List<LocalDateTime> r = CalculadoraHorarios.calcular(DIA, List.of(janela(8, 10)), List.of(),
            Duration.ofMinutes(60), PASSO, SEM_MINIMO, SEM_MAXIMO);
        assertEquals(List.of(h(8, 0), h(8, 30), h(9, 0)), r);
    }

    @Test
    void agendamentoRemoveHorariosQueSobrepoem() {
        // Ocupado 09:00-10:00; procedimento de 60 min na janela 08-12
        List<Intervalo> ocupados = List.of(new Intervalo(h(9, 0), h(10, 0)));
        List<LocalDateTime> r = CalculadoraHorarios.calcular(DIA, List.of(janela(8, 12)), ocupados,
            Duration.ofMinutes(60), PASSO, SEM_MINIMO, SEM_MAXIMO);
        // 08:30 acabaria às 09:30 (sobrepõe); 08:00 termina às 09:00 (encosta, pode); 10:00 em diante livre
        assertEquals(List.of(h(8, 0), h(10, 0), h(10, 30), h(11, 0)), r);
    }

    @Test
    void bloqueioFunciona() {
        List<Intervalo> ocupados = List.of(new Intervalo(h(8, 0), h(11, 0)));
        List<LocalDateTime> r = CalculadoraHorarios.calcular(DIA, List.of(janela(8, 12)), ocupados,
            Duration.ofMinutes(30), PASSO, SEM_MINIMO, SEM_MAXIMO);
        assertEquals(List.of(h(11, 0), h(11, 30)), r);
    }

    @Test
    void respeitaAntecedenciaMinima() {
        // "Agora" é 08:10 e a antecedência é 30 min => primeiro horário permitido: 08:40
        LocalDateTime minimo = h(8, 40);
        List<LocalDateTime> r = CalculadoraHorarios.calcular(DIA, List.of(janela(8, 10)), List.of(),
            Duration.ofMinutes(30), PASSO, minimo, SEM_MAXIMO);
        assertEquals(List.of(h(9, 0), h(9, 30)), r);
    }

    @Test
    void respeitaLimiteDeTrintaDias() {
        List<LocalDateTime> r = CalculadoraHorarios.calcular(DIA, List.of(janela(8, 10)), List.of(),
            Duration.ofMinutes(30), PASSO, SEM_MINIMO, h(8, 30));
        assertEquals(List.of(h(8, 0), h(8, 30)), r);
    }

    @Test
    void duasJanelasNoMesmoDia() {
        List<LocalDateTime> r = CalculadoraHorarios.calcular(DIA, List.of(janela(8, 9), janela(14, 15)), List.of(),
            Duration.ofMinutes(60), PASSO, SEM_MINIMO, SEM_MAXIMO);
        assertEquals(List.of(h(8, 0), h(14, 0)), r);
    }

    @Test
    void semJanelaNaoHaHorarios() {
        assertTrue(CalculadoraHorarios.calcular(DIA, List.of(), List.of(),
            Duration.ofMinutes(30), PASSO, SEM_MINIMO, SEM_MAXIMO).isEmpty());
    }

    @Test
    void procedimentoMaiorQueAJanelaNaoGeraHorarios() {
        assertTrue(CalculadoraHorarios.calcular(DIA, List.of(janela(8, 9)), List.of(),
            Duration.ofMinutes(90), PASSO, SEM_MINIMO, SEM_MAXIMO).isEmpty());
    }

    @Test
    void duracaoInvalidaLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> CalculadoraHorarios.calcular(DIA, List.of(janela(8, 9)),
            List.of(), Duration.ZERO, PASSO, SEM_MINIMO, SEM_MAXIMO));
    }

    @Test
    void intervalosQueSoEncostamNaoSobrepoem() {
        Intervalo a = new Intervalo(h(9, 0), h(10, 0));
        assertTrue(!a.sobrepoe(h(10, 0), h(11, 0)));
        assertTrue(!a.sobrepoe(h(8, 0), h(9, 0)));
        assertTrue(a.sobrepoe(h(9, 30), h(10, 30)));
    }

} // CalculadoraHorariosTest
