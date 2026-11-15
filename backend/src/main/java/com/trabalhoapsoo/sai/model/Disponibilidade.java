package com.trabalhoapsoo.sai.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

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
 * Classe responsável por representar um intervalo de atendimento
 * semanal do(a) profissional (ex.: segundas-feiras, das 08:00 às 12:00).
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Entity 
public class Disponibilidade {
    /* ===== ATRIBUTOS ===== */
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id; // (Para o BD) identificador da disponibilidade

    @ManyToOne
    @JoinColumn (name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private DayOfWeek diaSemana;

    @Column (nullable = false)
    private LocalTime horaInicio;

    @Column (nullable = false)
    private LocalTime horaFim;

    /* ===== MÉTODOS ===== */
    /** Construtor vazio exigido pelo Hibernate */
    protected Disponibilidade() {}

    /**
     * Construtor da classe
     * 
     * @param profissional profissional dono da disponibilidade
     * @param diaSemana dia da semana em que o intervalo vale
     * @param horaInicio hora em que o atendimento começa
     * @param horaFim hora em que o atendimento termina
     */
    public Disponibilidade(Profissional profissional, DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFim) {
        this.profissional = profissional;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    /* ===== GETTERS & SETTERS ===== */
    /**
     * Método para obter o id da disponibilidade
     * 
     * @return id da disponibilidade
     */
    public Long getId() {
        return id;
    }

    /**
     * Método para obter o(a) profissional dono(a) da disponibilidade
     * 
     * @return profissional
     */
    public Profissional getProfissional() {
        return profissional;
    }

    /**
     * Método para obter o dia da semana
     * 
     * @return dia da semana
     */
    public DayOfWeek getDiaSemana() {
        return diaSemana;
    }

    /**
     * Método para obter a hora de início do atendimento
     * 
     * @return hora de início
     */
    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    /**
     * Método para obter a hora de término do atendimento
     * 
     * @return hora de término
     */
    public LocalTime getHoraFim() {
        return horaFim;
    }

} // Disponibilidade
