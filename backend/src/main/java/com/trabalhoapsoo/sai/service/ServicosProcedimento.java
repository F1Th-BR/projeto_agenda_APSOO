package com.trabalhoapsoo.sai.service;

import com.trabalhoapsoo.sai.exceptions.RecursoNaoEncontradoException;
import com.trabalhoapsoo.sai.model.Procedimento;
import com.trabalhoapsoo.sai.model.Profissional;
import com.trabalhoapsoo.sai.repository.ProcedimentoRepository;

import java.math.BigDecimal;
import java.time.Duration;


import org.springframework.stereotype.Service;


/**
 * Classe de apoio para gerenciamento dos procedimentos
 * de um profissional
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Service
public class ServicosProcedimento {
    /* ===== ATRIBUTOS ===== */
    // Abstrai a tabela 'procedimento' no BD. Service delega a
    //  Este repositório (repository) as operações para buscar,
    // salvar, e remover um procedimento.
    private final ProcedimentoRepository repository;

    /* ===== MÉTODOS ===== */
    /* */
    public ServicosProcedimento(ProcedimentoRepository repository) {
        this.repository = repository;
    }

    // TODO: REVISAR MÉTODOS
    /**
     * Método para criar um procedimento (RF001)
     * 
     * Usa: inserirProcedimento
     * 
     * @param nomeProcedimento
     * @param duracaoProcedimento
     * @param preco
     * @param profissional
     */
    public Procedimento criar(String nomeProcedimento, Duration duracaoProcedimento, BigDecimal preco, Profissional profissional) {
        Procedimento p = new Procedimento(nomeProcedimento, duracaoProcedimento, preco, profissional);
        return  repository.save(p);
    }

    /* ===== MÉTODOS DE EDIÇÃO (RF003) ===== */
    // TODO: achar uma forma melhor de identificar o procedimento. No momento, é feito pelo nome
    /**
     * Método para atualizar a duração de um procedimento
     * 
     * @param id id do procedimento no BD
     * @param novaDuracao
     */
    public Procedimento atualizarDuracao(Long id, Duration novaDuracao) {
        Procedimento p = buscarID(id);
        p.setDuracaoProcedimento(novaDuracao);
        return repository.save(p);
    }

    /**
     * Método para atualizar o preço do procedimento
     * 
     * @param id id do procedimento no BD
     * @param novoPreco
     */
    public Procedimento atualizarPreco(Long id, BigDecimal novoPreco) {
        Procedimento p = buscarID(id);
        p.setPreco(novoPreco);
        return repository.save(p);
    }

    /**
     * Método para atualizar o nome do procedimento
     * 
     * @param id id do procedimento no BD
     * @param novoNome
     */
    public Procedimento atualizarNome(Long id, String novoNome) {
        Procedimento p = buscarID(id);
        p.setNomeProcedimento(novoNome);
        return repository.save(p);
    }
    /* ===== FIM MÉTODOS DE EDIÇÃO ===== */

    /**
     * Método para remover o procedimento (RF004) 
     * 
     * @param nomeProcedimento
     */
    public void remover(Long id) {
        Procedimento p = buscarID(id);
        repository.delete(p);
    }

    /* ===== MÉTODOS PRIVADOS ===== */
    private Procedimento buscarID(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento não encontrado: "+ id));
        
    }
} // ServicosProcedimento