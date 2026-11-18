package com.trabalhoapsoo.sai.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import com.trabalhoapsoo.sai.exceptions.ConflitoException;
import com.trabalhoapsoo.sai.exceptions.RecursoNaoEncontradoException;
import com.trabalhoapsoo.sai.exceptions.RequisicaoInvalidaException;
import com.trabalhoapsoo.sai.model.Disponibilidade;
import com.trabalhoapsoo.sai.model.Profissional;
import com.trabalhoapsoo.sai.repository.AgendamentoRepository;
import com.trabalhoapsoo.sai.repository.DisponibilidadeRepository;
import com.trabalhoapsoo.sai.repository.ProfissionalRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classe de apoio para gerenciamento dos dias e horários de
 * atendimento de um(a) profissional (RF005).
 * 
 * Todos os métodos recebem o id do(a) profissional logado(a) e só
 * operam sobre registros que pertencem a ele(a) (RNF007).
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Service
public class ServicosDisponibilidade {
    /* ===== ATRIBUTOS ===== */
    private final DisponibilidadeRepository repository; // acesso à tabela 'disponibilidade'
    private final ProfissionalRepository profissionalRepository; // para localizar o dono
    private final AgendamentoRepository agendamentoRepository; // para avisar de agendamentos futuros (UC002)
    private final Clock relogio;

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     * 
     * @param repository repositório de disponibilidades
     * @param profissionalRepository repositório de profissionais
     * @param agendamentoRepository repositório de agendamentos
     * @param relogio relógio do sistema
     */
    public ServicosDisponibilidade(DisponibilidadeRepository repository, ProfissionalRepository profissionalRepository,
                                   AgendamentoRepository agendamentoRepository, Clock relogio) {
        this.repository = repository;
        this.profissionalRepository = profissionalRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.relogio = relogio;
    }

    /**
     * Método para cadastrar um intervalo de atendimento.
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @param diaSemana dia da semana
     * @param horaInicio hora de início
     * @param horaFim hora de término
     * @return disponibilidade salva
     * @throws RequisicaoInvalidaException se o fim não for depois do início
     * @throws ConflitoException se o intervalo se sobrepuser a outro do mesmo dia
     */
    @Transactional
    public Disponibilidade criar(Long profissionalId, DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFim) {
        if (!horaFim.isAfter(horaInicio)) {
            throw new RequisicaoInvalidaException("A hora de término deve ser depois da hora de início.");
        }

        // Dois intervalos se sobrepõem quando cada um começa antes de o outro terminar.
        // Intervalos que apenas se encostam (08:00-12:00 e 12:00-18:00) são permitidos
        boolean sobrepoe = repository.findByProfissionalIdAndDiaSemana(profissionalId, diaSemana).stream()
            .anyMatch(d -> d.getHoraInicio().isBefore(horaFim) && d.getHoraFim().isAfter(horaInicio));
        if (sobrepoe) {
            throw new ConflitoException("Este intervalo se sobrepõe a outro já cadastrado para o mesmo dia.");
        }

        Profissional profissional = profissionalRepository.findById(profissionalId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado: " + profissionalId));
        return repository.save(new Disponibilidade(profissional, diaSemana, horaInicio, horaFim));
    }

    /**
     * Método para listar os intervalos de atendimento do(a) profissional,
     * ordenados por dia da semana (segunda a domingo) e hora de início.
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @return intervalos ordenados
     */
    @Transactional(readOnly = true)
    public List<Disponibilidade> listar(Long profissionalId) {
        // A ordenação é feita aqui porque o banco guarda o dia como texto
        // e ordenaria por ordem alfabética (FRIDAY antes de MONDAY)
        return repository.findByProfissionalId(profissionalId).stream()
            .sorted(Comparator.comparing(Disponibilidade::getDiaSemana)
                .thenComparing(Disponibilidade::getHoraInicio))
            .toList();
    }

    /**
     * Método para remover um intervalo de atendimento.
     * Agendamentos já marcados não são afetados.
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @param id id da disponibilidade
     * @throws RecursoNaoEncontradoException se não existir ou for de outro(a) profissional
     */
    @Transactional
    public void remover(Long profissionalId, Long id, boolean confirmar) {
        Disponibilidade d = repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Disponibilidade não encontrada: " + id));
        // Se for de outro(a) profissional, responde "não encontrada" para não revelar que existe
        if (!d.getProfissional().getId().equals(profissionalId)) {
            throw new RecursoNaoEncontradoException("Disponibilidade não encontrada: " + id);
        }

        // UC002 (3.1): se ainda há agendamentos futuros nesse intervalo, avisa antes de remover.
        // Com confirmar=true a remoção segue e os agendamentos já feitos continuam valendo.
        long afetados = agendamentoRepository.listarAtivosAPartirDe(profissionalId, LocalDateTime.now(relogio)).stream()
            .filter(a -> a.getInicioAgendamento().getDayOfWeek() == d.getDiaSemana()
                && !a.getInicioAgendamento().toLocalTime().isBefore(d.getHoraInicio())
                && a.getInicioAgendamento().toLocalTime().isBefore(d.getHoraFim()))
            .count();
        if (afetados > 0 && !confirmar) {
            throw new ConflitoException("Existem " + afetados + " agendamento(s) futuro(s) neste horário. "
                + "Cancele-os ou repita a remoção com confirmar=true para manter os agendamentos já feitos.");
        }
        repository.delete(d);
    }

} // ServicosDisponibilidade
