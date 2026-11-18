package com.trabalhoapsoo.sai.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.trabalhoapsoo.sai.agenda.CalculadoraHorarios;
import com.trabalhoapsoo.sai.agenda.CalculadoraHorarios.Intervalo;
import com.trabalhoapsoo.sai.agenda.CalculadoraHorarios.JanelaTrabalho;
import com.trabalhoapsoo.sai.exceptions.ConflitoException;
import com.trabalhoapsoo.sai.exceptions.RecursoNaoEncontradoException;
import com.trabalhoapsoo.sai.exceptions.RequisicaoInvalidaException;
import com.trabalhoapsoo.sai.model.Agendamento;
import com.trabalhoapsoo.sai.model.AtorAgendamento;
import com.trabalhoapsoo.sai.model.ContaCliente;
import com.trabalhoapsoo.sai.model.Empresa;
import com.trabalhoapsoo.sai.model.EstadoAgendamento;
import com.trabalhoapsoo.sai.model.Procedimento;
import com.trabalhoapsoo.sai.model.ProcedimentoLink;
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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classe de apoio com as regras de agendamento: horários livres, agendar,
 * remarcar e cancelar, tanto pela cliente (link público) quanto pelo(a)
 * profissional.
 * 
 * Regras principais:
 *  - RF050: antecedência mínima de 30 min para agendar;
 *  - RF051: só é possível agendar nos próximos 30 dias;
 *  - RF047/RN04: nunca dois agendamentos ativos sobrepostos (o banco garante);
 *  - RF049: cancelar libera o horário;
 *  - RF052: a cliente só cancela/remarca com antecedência mínima configurável.
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Service
public class ServicosAgendamento {
    /* ===== ATRIBUTOS ===== */
    private static final Duration ANTECEDENCIA_MINIMA = Duration.ofMinutes(30); // RF050
    private static final int JANELA_DIAS = 30; // RF051
    private static final Duration PASSO = Duration.ofMinutes(30); // intervalo entre horários oferecidos

    private final AgendamentoRepository agendamentoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final DisponibilidadeRepository disponibilidadeRepository;
    private final BloqueioRepository bloqueioRepository;
    private final NotificadorAgendamento notificador; // encaixe para o WhatsApp
    private final EmpresaRepository empresaRepository;
    private final ProcedimentoLinkRepository procedimentoLinkRepository;
    private final ContaClienteRepository contaClienteRepository;
    private final Clock relogio;

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     */
    public ServicosAgendamento(AgendamentoRepository agendamentoRepository,
                               ProfissionalRepository profissionalRepository,
                               ProcedimentoRepository procedimentoRepository,
                               DisponibilidadeRepository disponibilidadeRepository,
                               BloqueioRepository bloqueioRepository,
                               EmpresaRepository empresaRepository,
                               ProcedimentoLinkRepository procedimentoLinkRepository,
                               ContaClienteRepository contaClienteRepository,
                               NotificadorAgendamento notificador,
                               Clock relogio) {
        this.agendamentoRepository = agendamentoRepository;
        this.profissionalRepository = profissionalRepository;
        this.procedimentoRepository = procedimentoRepository;
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.bloqueioRepository = bloqueioRepository;
        this.empresaRepository = empresaRepository;
        this.procedimentoLinkRepository = procedimentoLinkRepository;
        this.contaClienteRepository = contaClienteRepository;
        this.notificador = notificador;
        this.relogio = relogio;
    }

    /* ===== TIPOS AUXILIARES ===== */
    /**
     * Resultado da identificação de um link público (RF039): o link é de um(a)
     * profissional individual ou de uma empresa.
     * 
     * @param tipo "PROFISSIONAL" ou "EMPRESA"
     * @param profissional dono do link, se for individual
     * @param empresa dona do link, se for de empresa
     */
    public record LinkResolvido(String tipo, Profissional profissional, Empresa empresa) {
    } // LinkResolvido

    /**
     * Item da lista de procedimentos de um link: um ou mais procedimentos
     * (de profissionais diferentes) agrupados sob o mesmo nome (RF018).
     * 
     * @param chave identificador do item (usado para "qualquer profissional")
     * @param nome nome mostrado à cliente
     * @param opcoes procedimentos (cada um de um(a) profissional) que formam o item
     */
    public record ItemLink(String chave, String nome, List<Procedimento> opcoes) {
    } // ItemLink

    /* ===== LINK PÚBLICO (CLIENTE) ===== */
    /**
     * Identifica o dono de um link público (RF027, RF039).
     * 
     * @param linkPublico código do link
     * @return dono do link
     * @throws RecursoNaoEncontradoException se o link não existir ou estiver desativado
     */
    @Transactional(readOnly = true)
    public LinkResolvido resolverLink(String linkPublico) {
        var prof = profissionalRepository.findByLinkPublico(linkPublico);
        if (prof.isPresent()) {
            return new LinkResolvido("PROFISSIONAL", prof.get(), null);
        }
        return empresaRepository.findByLinkPublico(linkPublico)
            .filter(Empresa::isLinkAtivo)
            .map(e -> new LinkResolvido("EMPRESA", null, e))
            .orElseThrow(() -> new RecursoNaoEncontradoException("Link de agendamento não encontrado."));
    }

    /**
     * Lista os itens de procedimento de um link (RF028, RF030). Em link de
     * profissional, cada procedimento ativo é um item; em link de empresa, só
     * aparecem os liberados pela empresa (RN02), agrupados pelo nome (RN09).
     * 
     * @param link dono do link
     * @return itens do link
     */
    @Transactional(readOnly = true)
    public List<ItemLink> itensDoLink(LinkResolvido link) {
        List<ItemLink> itens = new ArrayList<>();
        if (link.profissional() != null) {
            for (Procedimento p : procedimentoRepository.findByProfissionalIdAndAtivoTrue(link.profissional().getId())) {
                itens.add(new ItemLink(String.valueOf(p.getId()), p.getNomeProcedimento(), List.of(p)));
            }
            return itens;
        }
        Map<String, String> nomes = new LinkedHashMap<>();
        Map<String, List<Procedimento>> grupos = new LinkedHashMap<>();
        for (ProcedimentoLink l : procedimentoLinkRepository.listarVisiveis(link.empresa().getId())) {
            nomes.putIfAbsent(l.chave(), l.nomeExibido());
            grupos.computeIfAbsent(l.chave(), k -> new ArrayList<>()).add(l.getProcedimento());
        }
        grupos.forEach((chave, opcoes) -> itens.add(new ItemLink(chave, nomes.get(chave), opcoes)));
        return itens;
    }

    /**
     * Calcula os horários livres de um dia (RF005, RF032, RF035). Informe o
     * procedimento de um(a) profissional específico OU a 'chave' de um item de
     * empresa ("qualquer profissional disponível", RF031): nesse caso o resultado
     * reúne os horários livres de todos os profissionais do item.
     * 
     * @param linkPublico código do link
     * @param procedimentoId procedimento de um(a) profissional (opcional se houver chave)
     * @param chave chave do item de empresa (opcional se houver procedimentoId)
     * @param dia dia desejado
     * @return horários de início livres, em ordem
     * @throws RecursoNaoEncontradoException se o link ou procedimento não existirem
     * @throws RequisicaoInvalidaException se o dia estiver fora dos próximos 30 dias
     */
    @Transactional(readOnly = true)
    public List<LocalDateTime> horariosLivres(String linkPublico, Long procedimentoId, String chave, LocalDate dia) {
        LinkResolvido link = resolverLink(linkPublico);
        List<Procedimento> opcoes = opcoesDoLink(link, procedimentoId, chave);
        validarDia(dia);
        java.util.TreeSet<LocalDateTime> uniao = new java.util.TreeSet<>();
        for (Procedimento p : opcoes) {
            uniao.addAll(calcularHorarios(p.getProfissional().getId(), p.getDuracaoProcedimento(), dia, null));
        }
        return new ArrayList<>(uniao);
    }

    /**
     * Cria um agendamento pela cliente, a partir do link público (RF032–RF035).
     * Com "qualquer profissional disponível" (chave), o sistema escolhe o primeiro
     * profissional livre no horário (RF031).
     * 
     * @param linkPublico código do link
     * @param procedimentoId procedimento escolhido (ou nulo se usar chave)
     * @param chave chave do item de empresa (ou nula)
     * @param nomeCliente nome informado (ignorado se a cliente estiver logada)
     * @param telefoneCliente telefone informado (ignorado se a cliente estiver logada)
     * @param inicio horário escolhido
     * @param contaClienteId id da conta da cliente logada, ou nulo se sem login (RF034)
     * @return agendamento criado (contém o código do link único da cliente)
     * @throws ConflitoException se o horário não estiver mais livre
     */
    @Transactional
    public Agendamento agendarPelaCliente(String linkPublico, Long procedimentoId, String chave, String nomeCliente,
                                          String telefoneCliente, LocalDateTime inicio, Long contaClienteId) {
        LinkResolvido link = resolverLink(linkPublico);
        ContaCliente conta = null;
        if (contaClienteId != null) {
            conta = contaClienteRepository.findById(contaClienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada."));
            nomeCliente = conta.getNomeCompleto(); // RF034: usa os dados da conta
            telefoneCliente = conta.getTelefone();
        }
        exigirDadosDaCliente(nomeCliente, telefoneCliente);
        Agendamento a = montarAgendamento(link, procedimentoId, chave, nomeCliente, telefoneCliente, inicio,
            AtorAgendamento.CLIENTE, false);
        a.vincularConta(conta);
        return salvar(a);
    }

    /**
     * Consulta um agendamento pelo link único da cliente.
     * 
     * @param token código do link único
     * @return agendamento
     * @throws RecursoNaoEncontradoException se o código não existir
     */
    @Transactional(readOnly = true)
    public Agendamento buscarPorToken(String token) {
        return agendamentoRepository.findByTokenCliente(token)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado."));
    }

    /**
     * Cancela um agendamento pelo link único da cliente (RF036).
     * 
     * @param token código do link único
     * @return agendamento cancelado
     * @throws ConflitoException se estiver cancelado ou muito próximo do horário
     */
    @Transactional
    public Agendamento cancelarPelaCliente(String token) {
        Agendamento a = buscarPorToken(token);
        validarAntecedenciaDaCliente(a);
        return cancelar(a, AtorAgendamento.CLIENTE);
    }

    /**
     * Remarca um agendamento pelo link único da cliente (RF037).
     * 
     * @param token código do link único
     * @param novoInicio novo horário
     * @return agendamento remarcado
     * @throws ConflitoException se o novo horário não estiver livre
     */
    @Transactional
    public Agendamento remarcarPelaCliente(String token, LocalDateTime novoInicio) {
        Agendamento a = buscarPorToken(token);
        validarAntecedenciaDaCliente(a);
        exigirHorarioLivreParaCliente(a, novoInicio);
        return remarcar(a, novoInicio, AtorAgendamento.CLIENTE);
    }

    /* ===== CLIENTE COM CONTA (RF026) ===== */
    /**
     * Lista os agendamentos de uma conta de cliente.
     * 
     * @param contaClienteId id da conta logada
     * @return agendamentos da conta, do mais recente ao mais antigo
     */
    @Transactional(readOnly = true)
    public List<Agendamento> listarDaConta(Long contaClienteId) {
        return agendamentoRepository.findByContaClienteIdOrderByInicioAgendamentoDesc(contaClienteId);
    }

    /* ===== EMPRESA (RF021, RF022) ===== */
    /**
     * A empresa agenda como se fosse uma cliente (RF021, UC003). Só marca nos
     * horários que o(a) profissional abriu e respeita o prazo mínimo (RF052).
     * 
     * @param empresaId id da empresa logada
     * @param procedimentoId procedimento de um(a) profissional vinculado (ou nulo se usar chave)
     * @param chave chave do item (para "qualquer profissional"), ou nula
     * @param nomeCliente nome do(a) cliente
     * @param telefoneCliente telefone do(a) cliente
     * @param inicio horário escolhido
     * @return agendamento criado, com origem EMPRESA
     */
    @Transactional
    public Agendamento agendarPelaEmpresa(Long empresaId, Long procedimentoId, String chave, String nomeCliente,
                                          String telefoneCliente, LocalDateTime inicio) {
        Empresa empresa = empresaRepository.findById(empresaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa não encontrada."));
        exigirDadosDaCliente(nomeCliente, telefoneCliente);
        Agendamento a = montarAgendamento(new LinkResolvido("EMPRESA", null, empresa), procedimentoId, chave,
            nomeCliente, telefoneCliente, inicio, AtorAgendamento.EMPRESA, true);
        a.vincularEmpresa(empresa);
        return salvar(a);
    }

    /**
     * Lista os agendamentos criados pela empresa.
     * 
     * @param empresaId id da empresa logada
     * @return agendamentos criados por ela
     */
    @Transactional(readOnly = true)
    public List<Agendamento> listarCriadosPelaEmpresa(Long empresaId) {
        return agendamentoRepository.findByEmpresaIdOrderByInicioAgendamentoDesc(empresaId);
    }

    /**
     * Remarca um agendamento criado pela empresa (RF022), com as mesmas regras e prazos da cliente.
     * 
     * @param empresaId id da empresa logada
     * @param id id do agendamento
     * @param novoInicio novo horário
     * @return agendamento remarcado
     */
    @Transactional
    public Agendamento remarcarPelaEmpresa(Long empresaId, Long id, LocalDateTime novoInicio) {
        Agendamento a = buscarDaEmpresa(empresaId, id);
        validarAntecedenciaDaCliente(a);
        exigirHorarioLivreParaCliente(a, novoInicio);
        return remarcar(a, novoInicio, AtorAgendamento.EMPRESA);
    }

    /**
     * Cancela um agendamento criado pela empresa (RF022).
     * 
     * @param empresaId id da empresa logada
     * @param id id do agendamento
     * @return agendamento cancelado
     */
    @Transactional
    public Agendamento cancelarPelaEmpresa(Long empresaId, Long id) {
        Agendamento a = buscarDaEmpresa(empresaId, id);
        validarAntecedenciaDaCliente(a);
        return cancelar(a, AtorAgendamento.EMPRESA);
    }

    /* ===== PROFISSIONAL ===== */
    /**
     * Lista os agendamentos do(a) profissional em um período (RF007, RF011).
     * Inclui os cancelados, para o histórico.
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @param de primeiro dia (inclusive)
     * @param ate último dia (inclusive)
     * @return agendamentos do período, em ordem de horário
     */
    @Transactional(readOnly = true)
    public List<Agendamento> listar(Long profissionalId, LocalDate de, LocalDate ate) {
        if (ate.isBefore(de)) {
            throw new RequisicaoInvalidaException("A data final deve ser igual ou posterior à inicial.");
        }
        return agendamentoRepository.listarNoPeriodo(profissionalId, de.atStartOfDay(), ate.plusDays(1).atStartOfDay());
    }

    /**
     * Marca um atendimento manualmente (RF008). O(a) profissional pode marcar
     * fora das janelas de atendimento, mas não sobre outro agendamento nem
     * sobre um bloqueio, e não no passado.
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @param procedimentoId procedimento
     * @param nomeCliente nome da cliente
     * @param telefoneCliente telefone da cliente
     * @param inicio início do atendimento
     * @return agendamento criado
     */
    @Transactional
    public Agendamento agendarManual(Long profissionalId, Long procedimentoId, String nomeCliente,
                                     String telefoneCliente, LocalDateTime inicio) {
        Procedimento proc = procedimentoAtivoDe(profissionalId, procedimentoId);
        exigirLivreParaProfissional(profissionalId, inicio, inicio.plus(proc.getDuracaoProcedimento()), null);
        return salvar(new Agendamento(nomeCliente.trim(), telefoneCliente.trim(), proc, inicio,
            AtorAgendamento.PROFISSIONAL, agora()));
    }

    /**
     * Remarca um agendamento do(a) profissional (RF009).
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @param id id do agendamento
     * @param novoInicio novo horário
     * @return agendamento remarcado
     */
    @Transactional
    public Agendamento remarcarPeloProfissional(Long profissionalId, Long id, LocalDateTime novoInicio) {
        Agendamento a = buscarDoProfissional(profissionalId, id);
        exigirAtivo(a);
        exigirLivreParaProfissional(profissionalId, novoInicio, novoInicio.plus(a.getDuracaoAgendamento()), a.getId());
        return remarcar(a, novoInicio, AtorAgendamento.PROFISSIONAL);
    }

    /**
     * Cancela um agendamento do(a) profissional (RF010).
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @param id id do agendamento
     * @return agendamento cancelado
     */
    @Transactional
    public Agendamento cancelarPeloProfissional(Long profissionalId, Long id) {
        return cancelar(buscarDoProfissional(profissionalId, id), AtorAgendamento.PROFISSIONAL);
    }

    /* ===== MÉTODOS PRIVADOS ===== */
    private LocalDateTime agora() {
        return LocalDateTime.now(relogio);
    }

    // Busca o agendamento e confere o dono (se for de outro(a), responde "não encontrado")
    private Agendamento buscarDoProfissional(Long profissionalId, Long id) {
        Agendamento a = agendamentoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado: " + id));
        if (!a.getProfissional().getId().equals(profissionalId)) {
            throw new RecursoNaoEncontradoException("Agendamento não encontrado: " + id);
        }
        return a;
    }

    private Procedimento procedimentoAtivoDe(Long profissionalId, Long procedimentoId) {
        return procedimentoRepository.findById(procedimentoId)
            .filter(p -> p.isAtivo() && p.getProfissional().getId().equals(profissionalId))
            .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não encontrado: " + procedimentoId));
    }

    // RF051: só de hoje até 30 dias à frente
    private void validarDia(LocalDate dia) {
        LocalDate hoje = agora().toLocalDate();
        if (dia.isBefore(hoje) || dia.isAfter(hoje.plusDays(JANELA_DIAS))) {
            throw new RequisicaoInvalidaException("Só é possível agendar nos próximos " + JANELA_DIAS + " dias.");
        }
    }

    private void exigirAtivo(Agendamento a) {
        if (a.getEstado() == EstadoAgendamento.CANCELADO) {
            throw new ConflitoException("Este agendamento já foi cancelado.");
        }
    }

    // RF052: a cliente (e a empresa) só cancela/remarca com o prazo mínimo definido pelo(a) profissional
    private void validarAntecedenciaDaCliente(Agendamento a) {
        exigirAtivo(a);
        long horas = a.getProfissional().getPrazoCancelamentoHoras();
        if (agora().plusHours(horas).isAfter(a.getInicioAgendamento())) {
            throw new ConflitoException("Não é mais possível alterar este agendamento "
                + "(mínimo de " + horas + "h de antecedência). Entre em contato com o(a) profissional.");
        }
    }

    // Confere que o novo horário está entre os livres do(a) profissional (ignorando o próprio agendamento)
    private void exigirHorarioLivreParaCliente(Agendamento a, LocalDateTime novoInicio) {
        exigirAtivo(a);
        validarDia(novoInicio.toLocalDate());
        List<LocalDateTime> livres = calcularHorarios(a.getProfissional().getId(), a.getDuracaoAgendamento(),
            novoInicio.toLocalDate(), a.getId());
        if (!livres.contains(novoInicio)) {
            throw new ConflitoException("Esse horário não está disponível. Escolha outro.");
        }
    }

    private void exigirDadosDaCliente(String nome, String telefone) {
        if (nome == null || nome.isBlank() || telefone == null || telefone.isBlank()) {
            throw new RequisicaoInvalidaException("Informe nome e telefone para agendar.");
        }
    }

    private Agendamento buscarDaEmpresa(Long empresaId, Long id) {
        Agendamento a = agendamentoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado: " + id));
        if (a.getEmpresa() == null || !a.getEmpresa().getId().equals(empresaId)) {
            throw new RecursoNaoEncontradoException("Agendamento não encontrado: " + id);
        }
        return a;
    }

    // Procedimentos (um por profissional) que atendem ao pedido: um procedimento específico
    // OU todos os do item de empresa identificado pela chave ("qualquer profissional")
    private List<Procedimento> opcoesDoLink(LinkResolvido link, Long procedimentoId, String chave) {
        if (procedimentoId != null) {
            return List.of(procedimentoDoLink(link, procedimentoId));
        }
        if (chave == null || chave.isBlank() || link.empresa() == null) {
            throw new RequisicaoInvalidaException("Informe o procedimento (procedimentoId).");
        }
        List<Procedimento> opcoes = itensDoLink(link).stream()
            .filter(i -> i.chave().equals(chave.trim().toLowerCase(java.util.Locale.ROOT)))
            .flatMap(i -> i.opcoes().stream())
            .toList();
        if (opcoes.isEmpty()) {
            throw new RecursoNaoEncontradoException("Procedimento não encontrado no link.");
        }
        return opcoes;
    }

    // Procedimento que realmente pertence ao link (de profissional ou liberado pela empresa)
    private Procedimento procedimentoDoLink(LinkResolvido link, Long procedimentoId) {
        if (link.profissional() != null) {
            return procedimentoAtivoDe(link.profissional().getId(), procedimentoId);
        }
        return procedimentoLinkRepository.listarVisiveis(link.empresa().getId()).stream()
            .map(ProcedimentoLink::getProcedimento)
            .filter(p -> p.getId().equals(procedimentoId))
            .findFirst()
            .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não encontrado: " + procedimentoId));
    }

    // Escolhe o(a) profissional livre no horário, valida as regras e monta o agendamento (sem salvar)
    private Agendamento montarAgendamento(LinkResolvido link, Long procedimentoId, String chave, String nome,
                                          String telefone, LocalDateTime inicio, AtorAgendamento origem,
                                          boolean respeitarPrazoDaProfissional) {
        List<Procedimento> opcoes = opcoesDoLink(link, procedimentoId, chave);
        validarDia(inicio.toLocalDate());
        for (Procedimento p : opcoes) {
            // UC003 (6.1): a empresa não agenda dentro do prazo mínimo de cancelamento/remarcação
            if (respeitarPrazoDaProfissional
                && agora().plusHours(p.getProfissional().getPrazoCancelamentoHoras()).isAfter(inicio)) {
                continue;
            }
            List<LocalDateTime> livres = calcularHorarios(p.getProfissional().getId(), p.getDuracaoProcedimento(),
                inicio.toLocalDate(), null);
            if (livres.contains(inicio)) {
                return new Agendamento(nome.trim(), telefone.trim(), p, inicio, origem, agora());
            }
        }
        throw new ConflitoException("Esse horário não está disponível"
            + (respeitarPrazoDaProfissional ? " (ou está dentro do prazo mínimo de antecedência)" : "")
            + ". Escolha outro.");
    }

    // Monta os dados da CalculadoraHorarios (acessa o BD) e a executa
    private List<LocalDateTime> calcularHorarios(Long profissionalId, Duration duracao, LocalDate dia, Long ignorarAgendamentoId) {
        DayOfWeek diaSemana = dia.getDayOfWeek();
        List<JanelaTrabalho> janelas = disponibilidadeRepository.findByProfissionalIdAndDiaSemana(profissionalId, diaSemana)
            .stream().map(d -> new JanelaTrabalho(d.getHoraInicio(), d.getHoraFim())).toList();

        LocalDateTime inicioDia = dia.atStartOfDay();
        LocalDateTime fimDia = dia.plusDays(1).atStartOfDay();
        List<Intervalo> ocupados = new ArrayList<>();
        agendamentoRepository.buscarAtivosNoPeriodo(profissionalId, inicioDia, fimDia).stream()
            .filter(a -> !java.util.Objects.equals(a.getId(), ignorarAgendamentoId))
            .forEach(a -> ocupados.add(new Intervalo(a.getInicioAgendamento(), a.getFimAgendamento())));
        bloqueioRepository.findByProfissionalIdAndFimBloqueioAfterAndInicioBloqueioBefore(profissionalId, inicioDia, fimDia)
            .forEach(b -> ocupados.add(new Intervalo(b.getInicioBloqueio(), b.getFimBloqueio())));

        LocalDateTime agora = agora();
        return CalculadoraHorarios.calcular(dia, janelas, ocupados, duracao, PASSO,
            agora.plus(ANTECEDENCIA_MINIMA), agora.plusDays(JANELA_DIAS));
    }

    // Para o(a) profissional: só exige futuro, sem agendamento nem bloqueio no meio
    private void exigirLivreParaProfissional(Long profissionalId, LocalDateTime inicio, LocalDateTime fim, Long ignorarId) {
        if (!inicio.isAfter(agora())) {
            throw new RequisicaoInvalidaException("O horário deve estar no futuro.");
        }
        List<Intervalo> ocupados = new ArrayList<>();
        agendamentoRepository.buscarAtivosNoPeriodo(profissionalId, inicio, fim).stream()
            .filter(a -> !java.util.Objects.equals(a.getId(), ignorarId))
            .forEach(a -> ocupados.add(new Intervalo(a.getInicioAgendamento(), a.getFimAgendamento())));
        bloqueioRepository.findByProfissionalIdAndFimBloqueioAfterAndInicioBloqueioBefore(profissionalId, inicio, fim)
            .forEach(b -> ocupados.add(new Intervalo(b.getInicioBloqueio(), b.getFimBloqueio())));
        if (!CalculadoraHorarios.livre(inicio, fim, ocupados)) {
            throw new ConflitoException("Já existe um agendamento ou bloqueio nesse horário.");
        }
    }

    // saveAndFlush força o INSERT agora: se a restrição do banco (V5) barrar uma
    // corrida entre duas clientes, o erro vira 409 e não 500
    private Agendamento salvar(Agendamento a) {
        Agendamento salvo = agendamentoRepository.saveAndFlush(a);
        notificador.agendamentoCriado(salvo);
        return salvo;
    }

    private Agendamento cancelar(Agendamento a, AtorAgendamento ator) {
        exigirAtivo(a);
        a.cancelar(ator, agora());
        Agendamento salvo = agendamentoRepository.saveAndFlush(a);
        notificador.agendamentoCancelado(salvo);
        return salvo;
    }

    private Agendamento remarcar(Agendamento a, LocalDateTime novoInicio, AtorAgendamento ator) {
        a.remarcar(novoInicio, ator, agora());
        Agendamento salvo = agendamentoRepository.saveAndFlush(a);
        notificador.agendamentoRemarcado(salvo);
        return salvo;
    }

} // ServicosAgendamento
