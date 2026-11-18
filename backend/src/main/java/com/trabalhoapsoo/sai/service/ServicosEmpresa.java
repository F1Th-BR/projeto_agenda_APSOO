package com.trabalhoapsoo.sai.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.trabalhoapsoo.sai.dto.ProfissionalProcedimentosResponse;
import com.trabalhoapsoo.sai.dto.ProfissionalProcedimentosResponse.ProcedimentoNoLink;
import com.trabalhoapsoo.sai.exceptions.ConflitoException;
import com.trabalhoapsoo.sai.exceptions.RecursoNaoEncontradoException;
import com.trabalhoapsoo.sai.model.Agendamento;
import com.trabalhoapsoo.sai.model.Empresa;
import com.trabalhoapsoo.sai.model.EstadoVinculo;
import com.trabalhoapsoo.sai.model.Procedimento;
import com.trabalhoapsoo.sai.model.ProcedimentoLink;
import com.trabalhoapsoo.sai.model.Profissional;
import com.trabalhoapsoo.sai.model.VinculoEmpresa;
import com.trabalhoapsoo.sai.repository.EmpresaRepository;
import com.trabalhoapsoo.sai.repository.ProcedimentoLinkRepository;
import com.trabalhoapsoo.sai.repository.ProcedimentoRepository;
import com.trabalhoapsoo.sai.repository.ProfissionalRepository;
import com.trabalhoapsoo.sai.repository.VinculoEmpresaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classe de apoio com as ações da empresa (RF015–RF020).
 * 
 * A empresa NÃO cria, edita nem remove procedimentos, horários ou bloqueios dos
 * profissionais: ela só convida, controla o que aparece no link e consulta as
 * agendas em modo leitura.
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Service
public class ServicosEmpresa {
    /* ===== ATRIBUTOS ===== */
    private final EmpresaRepository empresaRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final VinculoEmpresaRepository vinculoRepository;
    private final ProcedimentoLinkRepository linkRepository;
    private final ServicosAgendamento servicosAgendamento;
    private final Clock relogio;

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     */
    public ServicosEmpresa(EmpresaRepository empresaRepository, ProfissionalRepository profissionalRepository,
                           ProcedimentoRepository procedimentoRepository, VinculoEmpresaRepository vinculoRepository,
                           ProcedimentoLinkRepository linkRepository, ServicosAgendamento servicosAgendamento,
                           Clock relogio) {
        this.empresaRepository = empresaRepository;
        this.profissionalRepository = profissionalRepository;
        this.procedimentoRepository = procedimentoRepository;
        this.vinculoRepository = vinculoRepository;
        this.linkRepository = linkRepository;
        this.servicosAgendamento = servicosAgendamento;
        this.relogio = relogio;
    }

    /* ----- PERFIL E LINK (RF016) ----- */
    /**
     * Consulta a empresa logada.
     * 
     * @param empresaId id da empresa
     * @return empresa
     */
    @Transactional(readOnly = true)
    public Empresa perfil(Long empresaId) {
        return buscar(empresaId);
    }

    /**
     * Gera um novo código de link, invalidando o anterior (RF016).
     * 
     * @param empresaId id da empresa
     * @return empresa com o novo link
     */
    @Transactional
    public Empresa regenerarLink(Long empresaId) {
        Empresa e = buscar(empresaId);
        e.regenerarLink();
        return empresaRepository.save(e);
    }

    /**
     * Ativa ou desativa o link (RF016).
     * 
     * @param empresaId id da empresa
     * @param ativo true para ativar
     * @return empresa atualizada
     */
    @Transactional
    public Empresa definirLinkAtivo(Long empresaId, boolean ativo) {
        Empresa e = buscar(empresaId);
        e.setLinkAtivo(ativo);
        return empresaRepository.save(e);
    }

    /* ----- CONVITES E VÍNCULOS (RF015) ----- */
    /**
     * Convida um(a) profissional já cadastrado(a) (RF015). O convite fica pendente
     * até a pessoa aceitar ou recusar (RF013).
     * 
     * @param empresaId id da empresa
     * @param emailProfissional e-mail da conta do(a) profissional
     * @return convite criado
     * @throws RecursoNaoEncontradoException se não houver profissional com esse e-mail
     * @throws ConflitoException se já houver convite em aberto ou vínculo ativo
     */
    @Transactional
    public VinculoEmpresa convidar(Long empresaId, String emailProfissional) {
        Empresa empresa = buscar(empresaId);
        Profissional prof = profissionalRepository.findByEmail(emailProfissional.trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new RecursoNaoEncontradoException("Não há profissional cadastrado(a) com esse e-mail."));
        boolean aberto = vinculoRepository.existsByEmpresaIdAndProfissionalIdAndStatus(empresaId, prof.getId(), EstadoVinculo.PENDENTE)
            || vinculoRepository.existsByEmpresaIdAndProfissionalIdAndStatus(empresaId, prof.getId(), EstadoVinculo.ATIVO);
        if (aberto) {
            throw new ConflitoException("Já existe um convite pendente ou vínculo ativo com essa pessoa.");
        }
        return vinculoRepository.save(new VinculoEmpresa(empresa, prof, LocalDateTime.now(relogio)));
    }

    /**
     * Lista os convites pendentes e os vínculos ativos da empresa.
     * 
     * @param empresaId id da empresa
     * @return convites pendentes seguidos dos vínculos ativos
     */
    @Transactional(readOnly = true)
    public List<VinculoEmpresa> listarVinculos(Long empresaId) {
        List<VinculoEmpresa> r = new ArrayList<>(vinculoRepository.findByEmpresaIdAndStatus(empresaId, EstadoVinculo.PENDENTE));
        r.addAll(vinculoRepository.findByEmpresaIdAndStatus(empresaId, EstadoVinculo.ATIVO));
        return r;
    }

    /* ----- PROCEDIMENTOS NO LINK (RF017–RF019) ----- */
    /**
     * Lista os profissionais vinculados com seus procedimentos e se cada um está
     * liberado no link (RF019).
     * 
     * @param empresaId id da empresa
     * @return procedimentos por profissional
     */
    @Transactional(readOnly = true)
    public List<ProfissionalProcedimentosResponse> procedimentosPorProfissional(Long empresaId) {
        List<ProfissionalProcedimentosResponse> r = new ArrayList<>();
        for (VinculoEmpresa v : vinculoRepository.findByEmpresaIdAndStatus(empresaId, EstadoVinculo.ATIVO)) {
            Profissional prof = v.getProfissional();
            List<ProcedimentoNoLink> itens = new ArrayList<>();
            for (Procedimento p : procedimentoRepository.findByProfissionalIdAndAtivoTrue(prof.getId())) {
                ProcedimentoLink l = linkRepository.findByEmpresaIdAndProcedimentoId(empresaId, p.getId()).orElse(null);
                itens.add(new ProcedimentoNoLink(p.getId(), p.getNomeProcedimento(), p.getDuracaoProcedimento().toMinutes(),
                    p.getPreco(), l != null && l.isAtivo(), l == null ? null : l.getNomeNoLink()));
            }
            r.add(new ProfissionalProcedimentosResponse(prof.getId(), prof.getNome(), itens));
        }
        return r;
    }

    /**
     * Libera um procedimento de um(a) profissional vinculado(a) no link da empresa, ou
     * atualiza o nome de agrupamento (RF017, RF018). Só a empresa faz essa ligação.
     * 
     * @param empresaId id da empresa
     * @param procedimentoId id do procedimento
     * @param nomeNoLink nome de agrupamento (nulo/vazio usa o nome do procedimento)
     * @return liberação salva
     * @throws RecursoNaoEncontradoException se o procedimento não for de um(a) profissional vinculado(a)
     */
    @Transactional
    public ProcedimentoLink liberar(Long empresaId, Long procedimentoId, String nomeNoLink) {
        Empresa empresa = buscar(empresaId);
        Procedimento p = procedimentoRepository.findById(procedimentoId)
            .filter(Procedimento::isAtivo)
            .filter(x -> vinculoRepository.existsByEmpresaIdAndProfissionalIdAndStatus(
                empresaId, x.getProfissional().getId(), EstadoVinculo.ATIVO))
            .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não encontrado: " + procedimentoId));
        String nome = (nomeNoLink == null || nomeNoLink.isBlank()) ? null : nomeNoLink.trim();
        ProcedimentoLink l = linkRepository.findByEmpresaIdAndProcedimentoId(empresaId, procedimentoId)
            .orElseGet(() -> new ProcedimentoLink(empresa, p, nome));
        l.setNomeNoLink(nome);
        l.setAtivo(true);
        return linkRepository.save(l);
    }

    /**
     * Tira um procedimento do link da empresa (o procedimento em si não é afetado).
     * 
     * @param empresaId id da empresa
     * @param procedimentoId id do procedimento
     */
    @Transactional
    public void desvincular(Long empresaId, Long procedimentoId) {
        ProcedimentoLink l = linkRepository.findByEmpresaIdAndProcedimentoId(empresaId, procedimentoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não está liberado no link."));
        l.setAtivo(false);
        linkRepository.save(l);
    }

    /* ----- AGENDAS (RF020) ----- */
    /**
     * Agenda de um(a) profissional vinculado(a), em modo SOMENTE LEITURA (RF020).
     * 
     * @param empresaId id da empresa
     * @param profissionalId id do(a) profissional
     * @param de primeiro dia
     * @param ate último dia
     * @return agendamentos do período
     * @throws RecursoNaoEncontradoException se a pessoa não for vinculada(a) à empresa
     */
    @Transactional(readOnly = true)
    public List<Agendamento> agendaDoProfissional(Long empresaId, Long profissionalId, LocalDate de, LocalDate ate) {
        if (!vinculoRepository.existsByEmpresaIdAndProfissionalIdAndStatus(empresaId, profissionalId, EstadoVinculo.ATIVO)) {
            throw new RecursoNaoEncontradoException("Profissional não vinculado(a) a esta empresa.");
        }
        return servicosAgendamento.listar(profissionalId, de, ate);
    }

    /* ===== MÉTODOS PRIVADOS ===== */
    private Empresa buscar(Long empresaId) {
        return empresaRepository.findById(empresaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa não encontrada."));
    }

} // ServicosEmpresa
