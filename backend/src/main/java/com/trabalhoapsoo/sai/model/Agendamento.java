package com.trabalhoapsoo.sai.model;

import java.time.Duration;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Classe responsável por estruturar um agendamento
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Entity 
public class Agendamento {
    /* ===== ATRIBUTOS ===== */
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id; // (Para o BD) id do agendamento

    @Column (nullable = false)
    private LocalDateTime inicioAgendamento;

    @Column (nullable = false)
    private LocalDateTime fimAgendamento;
    
    @Embedded 
    private DadosCliente cliente;

    @ManyToOne
    private Procedimento procedimento;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private EstadoAgendamento estado;

    /* ===== MÉTODOS ===== */
    /** Construtor vazio exigido pelo Hibernate */
    protected Agendamento() {}

    /**
     * Construtor da classe.
     * 
     * 
     * @param nomeCliente 
     * @param telefoneCliente
     * @param profissional 
     * @param procedimento
     */
    public Agendamento(String nomeCliente, String telefoneCliente, Procedimento procedimento, LocalDateTime inicioAgendamento) {
        this.cliente = new DadosCliente(nomeCliente, telefoneCliente);
        this.procedimento = procedimento;
        this.inicioAgendamento = inicioAgendamento;
        this.fimAgendamento = inicioAgendamento.plus(procedimento.getDuracaoProcedimento());
        this.estado = EstadoAgendamento.AGENDADO;
    }

    /* ===== GETTERS & SETTERS ===== */
    /**
     * Método para obter o id do agendamento
     * 
     * @return
     */
    public Long getId() {
        return id;
    }

    /**
     * Método para obter o início do agendamento
     * 
     * @return
     */
    public LocalDateTime getInicioAgendamento() {
        return inicioAgendamento;
    }

    /**
     * Método para obter o fim do agendamento
     * 
     * @return
     */
    public LocalDateTime getFimAgendamento() {
        return fimAgendamento;
    }

    /**
     * Método para obter o nome do(a) cliente
     * 
     * @return
     */
    public String getNomeCliente() {
        return cliente.getNome_cliente();
    }

    /**
     * Método para obter o nº de telefone do cliente
     * 
     * @return
     */
    public String getTelefoneCliente() {
        return cliente.getTelefone();
    }

    /**
     * Método para obter o procedimento a ser realizado
     * 
     * @return
     */
    public Procedimento getProcedimento() {
        return procedimento;
    }

    /**
     * Método para obter o estado do agendamento
     * 
     * @return
     */
    public EstadoAgendamento getEstado() {
        return estado;
    }

    /* ===== MÉTODOS AUXILIARES ===== */
    /**
     * Retorna a duração do AGENDAMENTO, calculada
     * a partir do tempo de início e tempo de término
     * registrado. Pode divergir caso o tempo do 
     * procedimento tenha sido alterado posteriormente
     * 
     * NOTA: Não confundir com o método da classe
     *       Procedimento que retorna a duração do
     *       procedimento
     * 
     * @return duração do agendamento
     */
    public Duration getduracaoAgendamento() {
        return Duration.between(inicioAgendamento, fimAgendamento);
    }


} // Agendamento
