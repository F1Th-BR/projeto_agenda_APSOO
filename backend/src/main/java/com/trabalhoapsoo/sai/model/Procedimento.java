package com.trabalhoapsoo.sai.model;

import java.math.BigDecimal;
import java.time.Duration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Classe responsável por definir procedimentos
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Entity 
public class Procedimento {
    /* ===== ATRIBUTOS ===== */
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id; // (Para o BD) identificador do procedimento

    @Column (nullable = false)
    private String nomeProcedimento;

    @Column (nullable = false)
    private Duration duracaoProcedimento;

    @Column (nullable = false)
    private BigDecimal preco;

    @ManyToOne
    @JoinColumn (name = "profissional_id", nullable = false)
    private Profissional profissional;

    /* ===== MÉTODOS ===== */
    /** Construtor vazio exigido pelo Hibernate */
    protected Procedimento() {}
    
    /**
     * Construtor da classe
     * 
     * @param nomeProcedimento
     * @param duracaoProcedimento
     * @param preco
     * @param profissional
     */
    public Procedimento(String nomeProcedimento, Duration duracaoProcedimento, BigDecimal preco, Profissional profissional) {
        this.nomeProcedimento = nomeProcedimento;
        this.duracaoProcedimento = duracaoProcedimento;
        this.preco = preco;
        this.profissional = profissional;
    }

    /* ===== GETTERS & SETTERS ===== */
    /**
     * Método que retorna o id do procedimento
     * 
     * @return id do procedimento
     */
    public Long getId() {
        return id;
    }
    
    /**
     * Método get para o nome do procedimento
     * @return nome do procedimento
     */
    public String getNomeProcedimento() {
        return nomeProcedimento;
    }

    /**
     * Método get para a duração do procedimento
     * @return
     */
    public Duration getDuracaoProcedimento() {
        return duracaoProcedimento;
    }

    /**
     * Método para retornar o preço do procedimento
     * 
     * @return preço do procedimento
     */
    public BigDecimal getPreco() {
        return preco;
    }

    /**
     * Método para obter a profissional que realiza esse
     * procedimento
     * 
     * @return profissional responsável
     */
    public Profissional getProfissional() {
        return profissional;
    }

    /**
     * Método para inserir a duração de um procedimento
     * 
     * @param duracaoProcedimento
     */
    public void setDuracaoProcedimento(Duration duracaoProcedimento) {
        this.duracaoProcedimento = duracaoProcedimento;
    }

    /**
     * Método para inserir o preço de um procedimento
     * 
     * @param preco
     */
    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    /**
     * Método para inserir o nome do procedimento
     * 
     * @param nomeProcedimento
     */
    public void setNomeProcedimento(String nomeProcedimento) {
        this.nomeProcedimento = nomeProcedimento;
    }

} // Procedimento
