package com.trabalhoapsoo.sai.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import com.trabalhoapsoo.sai.exceptions.ConflitoException;
import com.trabalhoapsoo.sai.exceptions.RecursoNaoEncontradoException;
import com.trabalhoapsoo.sai.exceptions.RequisicaoInvalidaException;
import com.trabalhoapsoo.sai.model.Bloqueio;
import com.trabalhoapsoo.sai.model.Profissional;
import com.trabalhoapsoo.sai.repository.AgendamentoRepository;
import com.trabalhoapsoo.sai.repository.BloqueioRepository;
import com.trabalhoapsoo.sai.repository.ProfissionalRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classe de apoio para gerenciamento dos bloqueios de agenda
 * de um(a) profissional (RF006).
 * 
 * Todos os métodos recebem o id do(a) profissional logado(a) e só
 * operam sobre registros que pertencem a ele(a) (RNF007).
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Service
public class ServicosBloqueio {
    /* ===== ATRIBUTOS ===== */
    private final BloqueioRepository repository; // acesso à tabela 'bloqueio'
    private final ProfissionalRepository profissionalRepository; // para localizar o dono
    private final AgendamentoRepository agendamentoRepository; // para checar agendamentos no período
    private final Clock relogio; // fornece o "agora" no fuso do sistema

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     * 
     * @param repository repositório de bloqueios
     * @param profissionalRepository repositório de profissionais
     * @param agendamentoRepository repositório de agendamentos
     * @param relogio relógio do sistema
     */
    public ServicosBloqueio(BloqueioRepository repository, ProfissionalRepository profissionalRepository,
                            AgendamentoRepository agendamentoRepository, Clock relogio) {
        this.repository = repository;
        this.profissionalRepository = profissionalRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.relogio = relogio;
    }

    /**
     * Método para cadastrar um bloqueio de agenda.
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @param inicio data e hora de início
     * @param fim data e hora de término
     * @param motivo motivo (pode ser nulo)
     * @return bloqueio salvo
     * @throws RequisicaoInvalidaException se o fim não for depois do início ou já tiver passado
     * @throws ConflitoException se houver agendamentos ativos no período
     */
    @Transactional
    public Bloqueio criar(Long profissionalId, LocalDateTime inicio, LocalDateTime fim, String motivo) {
        if (!fim.isAfter(inicio)) {
            throw new RequisicaoInvalidaException("O fim do bloqueio deve ser depois do início.");
        }
        if (!fim.isAfter(LocalDateTime.now(relogio))) {
            throw new RequisicaoInvalidaException("O bloqueio deve terminar em um horário futuro.");
        }

        // Não bloqueia um período que já tem clientes marcadas: o(a) profissional
        // precisa cancelar esses agendamentos antes, para ninguém ser pega de surpresa
        if (agendamentoRepository.contarAtivosNoPeriodo(profissionalId, inicio, fim) > 0) {
            throw new ConflitoException("Existem agendamentos nesse período. Cancele-os antes de bloquear.");
        }

        Profissional profissional = profissionalRepository.findById(profissionalId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado: " + profissionalId));
        String motivoLimpo = (motivo == null || motivo.isBlank()) ? null : motivo.trim();
        return repository.save(new Bloqueio(profissional, inicio, fim, motivoLimpo));
    }

    /**
     * Método para listar os bloqueios em andamento ou futuros do(a) profissional.
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @return bloqueios, do mais próximo para o mais distante
     */
    @Transactional(readOnly = true)
    public List<Bloqueio> listar(Long profissionalId) {
        return repository.findByProfissionalIdAndFimBloqueioAfterOrderByInicioBloqueioAsc(
            profissionalId, LocalDateTime.now(relogio));
    }

    /**
     * Método para remover um bloqueio, liberando o período de volta para agendamentos.
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @param id id do bloqueio
     * @throws RecursoNaoEncontradoException se não existir ou for de outro(a) profissional
     */
    @Transactional
    public void remover(Long profissionalId, Long id) {
        Bloqueio b = repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Bloqueio não encontrado: " + id));
        // Se for de outro(a) profissional, responde "não encontrado" para não revelar que existe
        if (!b.getProfissional().getId().equals(profissionalId)) {
            throw new RecursoNaoEncontradoException("Bloqueio não encontrado: " + id);
        }
        repository.delete(b);
    }

} // ServicosBloqueio
