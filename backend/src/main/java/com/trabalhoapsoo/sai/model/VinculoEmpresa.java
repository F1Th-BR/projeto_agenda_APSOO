package com.trabalhoapsoo.sai.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Classe responsável por estruturar o convite/vínculo entre uma empresa e
 * um(a) profissional (RF013, RF014, RF015, RF053).
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Entity
public class VinculoEmpresa {
    /* ===== ATRIBUTOS ===== */
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id; // (Para o BD) id do vínculo

    @ManyToOne
    @JoinColumn (name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne
    @JoinColumn (name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private EstadoVinculo status;

    @Column (nullable = false)
    private LocalDateTime criadoEm;

    @Column (nullable = false)
    private LocalDateTime atualizadoEm;

    /* ===== MÉTODOS ===== */
    /** Construtor vazio exigido pelo Hibernate */
    protected VinculoEmpresa() {}

    /**
     * Cria um convite pendente.
     * 
     * @param empresa empresa que convida
     * @param profissional profissional convidado(a)
     * @param agora momento do convite
     */
    public VinculoEmpresa(Empresa empresa, Profissional profissional, LocalDateTime agora) {
        this.empresa = empresa;
        this.profissional = profissional;
        this.status = EstadoVinculo.PENDENTE;
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    /**
     * Muda o estado do vínculo.
     * 
     * @param novo novo estado
     * @param agora momento da mudança
     */
    public void mudarEstado(EstadoVinculo novo, LocalDateTime agora) {
        this.status = novo;
        this.atualizadoEm = agora;
    }

    /* ===== GETTERS ===== */
    /** @return id do vínculo */
    public Long getId() { return id; }

    /** @return empresa */
    public Empresa getEmpresa() { return empresa; }

    /** @return profissional */
    public Profissional getProfissional() { return profissional; }

    /** @return estado atual */
    public EstadoVinculo getStatus() { return status; }

    /** @return momento do convite */
    public LocalDateTime getCriadoEm() { return criadoEm; }

    /** @return momento da última mudança */
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }

} // VinculoEmpresa
