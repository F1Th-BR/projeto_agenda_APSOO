package com.trabalhoapsoo.sai.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Classe responsável por representar um período em que o(a)
 * profissional não atende (folga, consulta, feriado...).
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Entity 
public class Bloqueio {
    /* ===== ATRIBUTOS ===== */
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id; // (Para o BD) identificador do bloqueio

    @ManyToOne
    @JoinColumn (name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Column (nullable = false)
    private LocalDateTime inicioBloqueio;

    @Column (nullable = false)
    private LocalDateTime fimBloqueio;

    @Column
    private String motivo; // opcional

    /* ===== MÉTODOS ===== */
    /** Construtor vazio exigido pelo Hibernate */
    protected Bloqueio() {}

    /**
     * Construtor da classe
     * 
     * @param profissional profissional dono do bloqueio
     * @param inicioBloqueio data e hora em que o bloqueio começa
     * @param fimBloqueio data e hora em que o bloqueio termina
     * @param motivo motivo do bloqueio (pode ser nulo)
     */
    public Bloqueio(Profissional profissional, LocalDateTime inicioBloqueio, LocalDateTime fimBloqueio, String motivo) {
        this.profissional = profissional;
        this.inicioBloqueio = inicioBloqueio;
        this.fimBloqueio = fimBloqueio;
        this.motivo = motivo;
    }

    /* ===== GETTERS & SETTERS ===== */
    /**
     * Método para obter o id do bloqueio
     * 
     * @return id do bloqueio
     */
    public Long getId() {
        return id;
    }

    /**
     * Método para obter o(a) profissional dono(a) do bloqueio
     * 
     * @return profissional
     */
    public Profissional getProfissional() {
        return profissional;
    }

    /**
     * Método para obter o início do bloqueio
     * 
     * @return data e hora de início
     */
    public LocalDateTime getInicioBloqueio() {
        return inicioBloqueio;
    }

    /**
     * Método para obter o fim do bloqueio
     * 
     * @return data e hora de término
     */
    public LocalDateTime getFimBloqueio() {
        return fimBloqueio;
    }

    /**
     * Método para obter o motivo do bloqueio
     * 
     * @return motivo, ou nulo se não foi informado
     */
    public String getMotivo() {
        return motivo;
    }

} // Bloqueio
