package com.trabalhoapsoo.sai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.trabalhoapsoo.sai.config.ConfigRelogio;
import com.trabalhoapsoo.sai.exceptions.ConflitoException;
import com.trabalhoapsoo.sai.model.Agendamento;
import com.trabalhoapsoo.sai.model.AtorAgendamento;
import com.trabalhoapsoo.sai.model.Disponibilidade;
import com.trabalhoapsoo.sai.model.EstadoAgendamento;
import com.trabalhoapsoo.sai.model.Procedimento;
import com.trabalhoapsoo.sai.model.Profissional;
import com.trabalhoapsoo.sai.notificacao.NotificadorAgendamento;
import com.trabalhoapsoo.sai.repository.AgendamentoRepository;
import com.trabalhoapsoo.sai.repository.BloqueioRepository;
import com.trabalhoapsoo.sai.repository.ContaClienteRepository;
import com.trabalhoapsoo.sai.repository.DisponibilidadeRepository;
import com.trabalhoapsoo.sai.repository.EmpresaRepository;
import com.trabalhoapsoo.sai.repository.ProcedimentoLinkRepository;
import com.trabalhoapsoo.sai.repository.ProcedimentoRepository;
import com.trabalhoapsoo.sai.repository.ProfissionalRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Testes das regras de agendamento (RF031–RF037, RF047–RF052), sem banco de
 * dados: os repositórios são simulados com Mockito.
 * 
 * Cenário: hoje é segunda 2026-11-16, 09:00. A profissional atende às terças,
 * das 08:00 às 12:00, e o procedimento dura 60 minutos.
 * 
 * @author Thiago Feijó de Albuquerque
 */
class ServicosAgendamentoTest {
    private static final LocalDate TERCA = LocalDate.of(2026, 11, 17);

    private AgendamentoRepository agendamentos;
    private BloqueioRepository bloqueios;
    private NotificadorAgendamento notificador;
    private Profissional profissional;
    private Procedimento procedimento;

    @BeforeEach
    void preparar() {
        agendamentos = mock(AgendamentoRepository.class);
        bloqueios = mock(BloqueioRepository.class);
        notificador = mock(NotificadorAgendamento.class);

        profissional = new Profissional("ana@x.com", "hash", "Ana", null);
        ReflectionTestUtils.setField(profissional, "id", 1L);
        procedimento = new Procedimento("Unha", Duration.ofMinutes(60), new BigDecimal("50.00"), profissional);
        ReflectionTestUtils.setField(procedimento, "id", 10L);

        when(agendamentos.buscarAtivosNoPeriodo(any(), any(), any())).thenReturn(List.of());
        when(bloqueios.findByProfissionalIdAndFimBloqueioAfterAndInicioBloqueioBefore(any(), any(), any())).thenReturn(List.of());
        when(agendamentos.saveAndFlush(any(Agendamento.class))).thenAnswer(i -> i.getArgument(0));
    }

    private ServicosAgendamento servico(LocalDateTime agora) {
        ProfissionalRepository profissionais = mock(ProfissionalRepository.class);
        when(profissionais.findByLinkPublico(profissional.getLinkPublico())).thenReturn(Optional.of(profissional));
        ProcedimentoRepository procedimentos = mock(ProcedimentoRepository.class);
        when(procedimentos.findById(10L)).thenReturn(Optional.of(procedimento));
        DisponibilidadeRepository disponibilidades = mock(DisponibilidadeRepository.class);
        when(disponibilidades.findByProfissionalIdAndDiaSemana(1L, DayOfWeek.TUESDAY))
            .thenReturn(List.of(new Disponibilidade(profissional, DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));
        when(disponibilidades.findByProfissionalIdAndDiaSemana(1L, DayOfWeek.MONDAY)).thenReturn(List.of());

        Clock relogio = Clock.fixed(agora.atZone(ConfigRelogio.FUSO_SISTEMA).toInstant(), ConfigRelogio.FUSO_SISTEMA);
        return new ServicosAgendamento(agendamentos, profissionais, procedimentos, disponibilidades, bloqueios,
            mock(EmpresaRepository.class), mock(ProcedimentoLinkRepository.class), mock(ContaClienteRepository.class),
            notificador, relogio);
    }

    private ServicosAgendamento servico() {
        return servico(LocalDateTime.of(2026, 11, 16, 9, 0));
    }

    @Test
    void agendaEmHorarioLivreERegistraAuditoria() {
        Agendamento a = servico().agendarPelaCliente(profissional.getLinkPublico(), 10L, null, "Maria", "9199999",
            TERCA.atTime(9, 0), null);

        assertEquals(EstadoAgendamento.AGENDADO, a.getEstado());
        assertEquals(AtorAgendamento.CLIENTE, a.getOrigem());
        assertEquals(TERCA.atTime(10, 0), a.getFimAgendamento());
        assertNotNull(a.getTokenCliente());
        verify(notificador).agendamentoCriado(a);
    }

    @Test
    void horarioJaOcupadoLancaConflito() {
        Agendamento existente = new Agendamento("Joana", "9188888", procedimento, TERCA.atTime(9, 0),
            AtorAgendamento.PROFISSIONAL, LocalDateTime.of(2026, 11, 16, 8, 0));
        when(agendamentos.buscarAtivosNoPeriodo(any(), any(), any())).thenReturn(List.of(existente));

        assertThrows(ConflitoException.class, () -> servico().agendarPelaCliente(profissional.getLinkPublico(),
            10L, null, "Maria", "9199999", TERCA.atTime(9, 0), null));
        verify(agendamentos, never()).saveAndFlush(any(Agendamento.class));
    }

    @Test
    void foraDoExpedienteLancaConflito() {
        assertThrows(ConflitoException.class, () -> servico().agendarPelaCliente(profissional.getLinkPublico(),
            10L, null, "Maria", "9199999", TERCA.atTime(13, 0), null));
    }

    @Test
    void naoOfereceHorarioComMenosDeTrintaMinutos() {
        // Terça 08:10 => antecedência mínima até 08:40; o primeiro horário de 60 min é 09:00
        List<LocalDateTime> livres = servico(TERCA.atTime(8, 10))
            .horariosLivres(profissional.getLinkPublico(), 10L, null, TERCA);
        assertFalse(livres.contains(TERCA.atTime(8, 0)));
        assertFalse(livres.contains(TERCA.atTime(8, 30)));
        assertEquals(TERCA.atTime(9, 0), livres.get(0));
    }

    @Test
    void clienteNaoCancelaDentroDoPrazo() {
        // Atendimento terça 09:00; "agora" é terça 08:00 e o prazo é 2h
        Agendamento a = new Agendamento("Maria", "9199999", procedimento, TERCA.atTime(9, 0),
            AtorAgendamento.CLIENTE, LocalDateTime.of(2026, 11, 16, 9, 0));
        when(agendamentos.findByTokenCliente(a.getTokenCliente())).thenReturn(Optional.of(a));

        assertThrows(ConflitoException.class, () -> servico(TERCA.atTime(8, 0)).cancelarPelaCliente(a.getTokenCliente()));
        assertEquals(EstadoAgendamento.AGENDADO, a.getEstado());
    }

    @Test
    void profissionalCancelaELiberaOHorario() {
        Agendamento a = new Agendamento("Maria", "9199999", procedimento, TERCA.atTime(9, 0),
            AtorAgendamento.CLIENTE, LocalDateTime.of(2026, 11, 16, 9, 0));
        ReflectionTestUtils.setField(a, "id", 5L);
        when(agendamentos.findById(5L)).thenReturn(Optional.of(a));

        Agendamento cancelado = servico().cancelarPeloProfissional(1L, 5L);

        assertEquals(EstadoAgendamento.CANCELADO, cancelado.getEstado());
        assertEquals(AtorAgendamento.PROFISSIONAL, cancelado.getCanceladoPor());
        assertTrue(cancelado.getCanceladoEm() != null);
        verify(notificador).agendamentoCancelado(cancelado);
    }

} // ServicosAgendamentoTest
