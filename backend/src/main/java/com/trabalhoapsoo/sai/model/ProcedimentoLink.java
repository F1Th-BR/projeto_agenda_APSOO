package com.trabalhoapsoo.sai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Classe responsável por estruturar a liberação de um procedimento de um(a)
 * profissional no link da empresa (RF017, RF018, RN02, RN09).
 * Procedimentos com o mesmo 'nomeNoLink' aparecem como um único item.
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Entity
public class ProcedimentoLink {
    /* ===== ATRIBUTOS ===== */
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id; // (Para o BD) id da liberação

    @ManyToOne
    @JoinColumn (name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne
    @JoinColumn (name = "procedimento_id", nullable = false)
    private Procedimento procedimento;

    private String nomeNoLink; // nome de agrupamento (opcional)

    @Column (nullable = false)
    private boolean ativo = true;

    /* ===== MÉTODOS ===== */
    /** Construtor vazio exigido pelo Hibernate */
    protected ProcedimentoLink() {}

    /**
     * Construtor da classe
     * 
     * @param empresa empresa dona do link
     * @param procedimento procedimento liberado
     * @param nomeNoLink nome de agrupamento (pode ser nulo)
     */
    public ProcedimentoLink(Empresa empresa, Procedimento procedimento, String nomeNoLink) {
        this.empresa = empresa;
        this.procedimento = procedimento;
        this.nomeNoLink = nomeNoLink;
    }

    /**
     * Define o nome de agrupamento (RF018).
     * 
     * @param nomeNoLink novo nome (nulo volta a usar o nome do procedimento)
     */
    public void setNomeNoLink(String nomeNoLink) {
        this.nomeNoLink = nomeNoLink;
    }

    /**
     * Ativa ou desativa a liberação.
     * 
     * @param ativo true para ativar
     */
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    /**
     * Chave de agrupamento: dois procedimentos com a mesma chave viram
     * um único item no link, ignorando maiúsculas e espaços nas pontas.
     * 
     * @return chave de agrupamento
     */
    public String chave() {
        return nomeExibido().trim().toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Nome mostrado à cliente: o de agrupamento, se houver, ou o do procedimento.
     * 
     * @return nome do item no link
     */
    public String nomeExibido() {
        return (nomeNoLink == null || nomeNoLink.isBlank()) ? procedimento.getNomeProcedimento() : nomeNoLink;
    }

    /* ===== GETTERS ===== */
    /** @return id da liberação */
    public Long getId() { return id; }

    /** @return empresa */
    public Empresa getEmpresa() { return empresa; }

    /** @return procedimento liberado */
    public Procedimento getProcedimento() { return procedimento; }

    /** @return nome de agrupamento (ou nulo) */
    public String getNomeNoLink() { return nomeNoLink; }

    /** @return true se a liberação está ativa */
    public boolean isAtivo() { return ativo; }

} // ProcedimentoLink
