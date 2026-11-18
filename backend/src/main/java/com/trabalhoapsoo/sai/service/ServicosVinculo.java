package com.trabalhoapsoo.sai.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import com.trabalhoapsoo.sai.exceptions.ConflitoException;
import com.trabalhoapsoo.sai.exceptions.RecursoNaoEncontradoException;
import com.trabalhoapsoo.sai.model.EstadoVinculo;
import com.trabalhoapsoo.sai.model.ProcedimentoLink;
import com.trabalhoapsoo.sai.model.VinculoEmpresa;
import com.trabalhoapsoo.sai.repository.ProcedimentoLinkRepository;
import com.trabalhoapsoo.sai.repository.VinculoEmpresaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classe de apoio com as ações do(a) PROFISSIONAL sobre convites de empresa:
 * aceitar, recusar e sair (RF013, RF014, RF053, RN08).
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Service
public class ServicosVinculo {
    /* ===== ATRIBUTOS ===== */
    private final VinculoEmpresaRepository vinculoRepository;
    private final ProcedimentoLinkRepository linkRepository;
    private final Clock relogio;

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     * 
     * @param vinculoRepository repositório de vínculos
     * @param linkRepository repositório de liberações no link
     * @param relogio relógio do sistema
     */
    public ServicosVinculo(VinculoEmpresaRepository vinculoRepository, ProcedimentoLinkRepository linkRepository, Clock relogio) {
        this.vinculoRepository = vinculoRepository;
        this.linkRepository = linkRepository;
        this.relogio = relogio;
    }

    /**
     * Lista os convites pendentes do(a) profissional.
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @return convites pendentes
     */
    @Transactional(readOnly = true)
    public List<VinculoEmpresa> convitesPendentes(Long profissionalId) {
        return vinculoRepository.findByProfissionalIdAndStatus(profissionalId, EstadoVinculo.PENDENTE);
    }

    /**
     * Retorna o vínculo ativo do(a) profissional, se houver.
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @return vínculo ativo
     * @throws RecursoNaoEncontradoException se não pertence a nenhuma empresa
     */
    @Transactional(readOnly = true)
    public VinculoEmpresa vinculoAtual(Long profissionalId) {
        return vinculoRepository.findFirstByProfissionalIdAndStatus(profissionalId, EstadoVinculo.ATIVO)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Você não está vinculado(a) a nenhuma empresa."));
    }

    /**
     * Aceita um convite (RF013): cria o vínculo ativo.
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @param vinculoId id do convite
     * @return vínculo ativo
     * @throws ConflitoException se o convite não estiver pendente ou se já estiver em outra empresa
     */
    @Transactional
    public VinculoEmpresa aceitar(Long profissionalId, Long vinculoId) {
        VinculoEmpresa v = buscarPendente(profissionalId, vinculoId);
        if (vinculoRepository.findFirstByProfissionalIdAndStatus(profissionalId, EstadoVinculo.ATIVO).isPresent()) {
            throw new ConflitoException("Você já faz parte de uma empresa. Saia dela antes de aceitar outro convite.");
        }
        v.mudarEstado(EstadoVinculo.ATIVO, LocalDateTime.now(relogio));
        return vinculoRepository.save(v);
    }

    /**
     * Recusa um convite (RF013): nenhum vínculo é criado.
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @param vinculoId id do convite
     * @return convite recusado
     */
    @Transactional
    public VinculoEmpresa recusar(Long profissionalId, Long vinculoId) {
        VinculoEmpresa v = buscarPendente(profissionalId, vinculoId);
        v.mudarEstado(EstadoVinculo.RECUSADO, LocalDateTime.now(relogio));
        return vinculoRepository.save(v);
    }

    /**
     * Sai da empresa (RF014). Os vínculos com o link da empresa são desativados
     * (RF053, RN08), mas conta, agenda, agendamentos e procedimentos continuam intactos.
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @return vínculo encerrado
     */
    @Transactional
    public VinculoEmpresa sair(Long profissionalId) {
        VinculoEmpresa v = vinculoAtual(profissionalId);
        v.mudarEstado(EstadoVinculo.ENCERRADO, LocalDateTime.now(relogio));
        for (ProcedimentoLink l : linkRepository.findByProcedimentoProfissionalIdAndAtivoTrue(profissionalId)) {
            l.setAtivo(false);
            linkRepository.save(l);
        }
        return vinculoRepository.save(v);
    }

    /* ===== MÉTODOS PRIVADOS ===== */
    // Busca o convite e confere o(a) dono(a) (de outra pessoa => "não encontrado")
    private VinculoEmpresa buscarPendente(Long profissionalId, Long vinculoId) {
        VinculoEmpresa v = vinculoRepository.findById(vinculoId)
            .filter(x -> x.getProfissional().getId().equals(profissionalId))
            .orElseThrow(() -> new RecursoNaoEncontradoException("Convite não encontrado: " + vinculoId));
        if (v.getStatus() != EstadoVinculo.PENDENTE) {
            throw new ConflitoException("Este convite não está mais pendente.");
        }
        return v;
    }

} // ServicosVinculo
