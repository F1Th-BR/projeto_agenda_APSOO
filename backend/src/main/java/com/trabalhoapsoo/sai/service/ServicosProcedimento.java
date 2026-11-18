package com.trabalhoapsoo.sai.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import com.trabalhoapsoo.sai.exceptions.ConflitoException;
import com.trabalhoapsoo.sai.exceptions.RecursoNaoEncontradoException;
import com.trabalhoapsoo.sai.model.Procedimento;
import com.trabalhoapsoo.sai.model.Profissional;
import com.trabalhoapsoo.sai.repository.ProcedimentoLinkRepository;
import com.trabalhoapsoo.sai.repository.ProcedimentoRepository;
import com.trabalhoapsoo.sai.repository.ProfissionalRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classe de apoio para gerenciamento dos procedimentos
 * de um profissional.
 * 
 * Todos os métodos recebem o id do(a) profissional logado(a) e só
 * operam sobre procedimentos que pertencem a ele(a) (RNF007).
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Service
public class ServicosProcedimento {
    /* ===== ATRIBUTOS ===== */
    // Abstrai a tabela 'procedimento' no BD. Service delega a
    // este repositório (repository) as operações para buscar,
    // salvar, e remover um procedimento.
    private final ProcedimentoRepository repository;

    private final ProfissionalRepository profissionalRepository; // para localizar o dono do procedimento
    private final ProcedimentoLinkRepository linkRepository; // para checar se está liberado no link de empresa (RF004)

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     * 
     * @param repository repositório de procedimentos
     * @param profissionalRepository repositório de profissionais
     * @param linkRepository repositório das liberações no link de empresa
     */
    public ServicosProcedimento(ProcedimentoRepository repository,
                                ProfissionalRepository profissionalRepository,
                                ProcedimentoLinkRepository linkRepository) {
        this.repository = repository;
        this.profissionalRepository = profissionalRepository;
        this.linkRepository = linkRepository;
    }

    /**
     * Método para criar um procedimento (RF002).
     * O procedimento nasce vinculado a quem o criou (RN01).
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @param nomeProcedimento nome do procedimento
     * @param duracaoProcedimento duração do procedimento
     * @param preco preço do procedimento
     * @return procedimento salvo
     */
    @Transactional
    public Procedimento criar(Long profissionalId, String nomeProcedimento, Duration duracaoProcedimento, BigDecimal preco) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado: " + profissionalId));
        return repository.save(new Procedimento(nomeProcedimento, duracaoProcedimento, preco, profissional));
    }

    /**
     * Método para listar os procedimentos do(a) profissional logado(a).
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @return procedimentos do(a) profissional
     */
    @Transactional(readOnly = true)
    public List<Procedimento> listar(Long profissionalId) {
        return repository.findByProfissionalIdAndAtivoTrue(profissionalId);
    }

    /**
     * Método para buscar um procedimento do(a) profissional logado(a).
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @param id id do procedimento no BD
     * @return procedimento encontrado
     * @throws RecursoNaoEncontradoException se não existir ou for de outro(a) profissional
     */
    @Transactional(readOnly = true)
    public Procedimento buscar(Long profissionalId, Long id) {
        return buscarDoProfissional(profissionalId, id);
    }

    /**
     * Método para editar um procedimento (RF003).
     * Agendamentos já realizados guardam o próprio horário de fim, então
     * não são afetados pela edição.
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @param id id do procedimento no BD
     * @param novoNome novo nome
     * @param novaDuracao nova duração
     * @param novoPreco novo preço
     * @return procedimento atualizado
     */
    @Transactional
    public Procedimento atualizar(Long profissionalId, Long id, String novoNome, Duration novaDuracao, BigDecimal novoPreco) {
        Procedimento p = buscarDoProfissional(profissionalId, id);
        p.setNomeProcedimento(novoNome);
        p.setDuracaoProcedimento(novaDuracao);
        p.setPreco(novoPreco);
        return repository.save(p);
    }

    /**
     * Método para remover o procedimento (RF004).
     * A remoção é lógica: o procedimento deixa de aparecer e de ser agendável,
     * mas os agendamentos já feitos continuam apontando para ele.
     * 
     * @param profissionalId id do(a) profissional logado(a)
     * @param id id do procedimento no BD
     * @throws ConflitoException se o procedimento estiver liberado no link de uma empresa
     */
    @Transactional
    public void remover(Long profissionalId, Long id) {
        Procedimento p = buscarDoProfissional(profissionalId, id);
        // RF004: se está liberado no link de uma empresa, é preciso pedir a ela que o desvincule antes
        if (linkRepository.existsByProcedimentoIdAndAtivoTrue(id)) {
            throw new ConflitoException("Este procedimento está liberado no link de uma empresa. "
                + "Peça à empresa para desvinculá-lo (ou saia da empresa) antes de removê-lo.");
        }
        p.desativar();
        repository.save(p);
    }

    /* ===== MÉTODOS PRIVADOS ===== */
    // Busca o procedimento e confere o dono. Se for de outro(a) profissional,
    // devolve "não encontrado" (e não "proibido") para não revelar que ele existe
    private Procedimento buscarDoProfissional(Long profissionalId, Long id) {
        Procedimento p = repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não encontrado: " + id));
        if (!p.isAtivo() || !p.getProfissional().getId().equals(profissionalId)) {
            throw new RecursoNaoEncontradoException("Procedimento não encontrado: " + id);
        }
        return p;
    }
} // ServicosProcedimento
