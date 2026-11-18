package com.trabalhoapsoo.sai.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
    @JoinColumn(name = "procedimento_id", nullable = false)
    private Procedimento procedimento;

    // Profissional dono do horário. Derivado do procedimento no construtor e
    // guardado aqui para o banco impedir sobreposição de horários (V5, RF047)
    @ManyToOne
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Enumerated (EnumType.STRING)
    @Column (name = "status",nullable = false)
    private EstadoAgendamento estado;

    // Código do link único que a cliente recebe para remarcar e cancelar
    // sem precisar de login (RF036, RF037). Aleatório e impossível de adivinhar
    @Column (nullable = false, unique = true, updatable = false)
    private String tokenCliente;

    // Conta da cliente, se ela estava logada ao agendar (RF026). Nulo = sem login
    @ManyToOne
    @JoinColumn (name = "conta_cliente_id")
    private ContaCliente contaCliente;

    // Empresa que criou o agendamento (RF021/RF022). Nulo = não foi criado por empresa
    @ManyToOne
    @JoinColumn (name = "empresa_id")
    private Empresa empresa;

    /* ----- Auditoria (RF045) ----- */
    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private AtorAgendamento origem; // quem criou o agendamento

    @Column (nullable = false)
    private LocalDateTime criadoEm;

    @Enumerated (EnumType.STRING)
    private AtorAgendamento remarcadoPor; // nulo se nunca foi remarcado

    private LocalDateTime remarcadoEm;

    @Enumerated (EnumType.STRING)
    private AtorAgendamento canceladoPor; // nulo se não foi cancelado

    private LocalDateTime canceladoEm;

    /* ===== MÉTODOS ===== */
    /** Construtor vazio exigido pelo Hibernate */
    protected Agendamento() {}

    /**
     * Construtor da classe. O horário de término é calculado a partir da
     * duração do procedimento, e o profissional é o dono do procedimento.
     * 
     * @param nomeCliente nome da cliente
     * @param telefoneCliente telefone/WhatsApp da cliente
     * @param procedimento procedimento a ser realizado
     * @param inicioAgendamento data e hora de início
     * @param origem quem está criando o agendamento
     * @param agora momento da criação (usado na auditoria)
     * @throws NullPointerException se algum dos parâmetros for nulo
     */
    public Agendamento(String nomeCliente, String telefoneCliente, Procedimento procedimento,
                       LocalDateTime inicioAgendamento, AtorAgendamento origem, LocalDateTime agora) {
        Objects.requireNonNull(nomeCliente, "nomeCliente");
        Objects.requireNonNull(telefoneCliente, "telefoneCliente");
        Objects.requireNonNull(procedimento, "procedimento");
        Objects.requireNonNull(inicioAgendamento, "inicioAgendamento");
        Objects.requireNonNull(origem, "origem");
        Objects.requireNonNull(agora, "agora");

        this.cliente = new DadosCliente(nomeCliente, telefoneCliente);
        this.procedimento = procedimento;
        this.profissional = procedimento.getProfissional();
        this.inicioAgendamento = inicioAgendamento;
        this.fimAgendamento = inicioAgendamento.plus(procedimento.getDuracaoProcedimento());
        this.estado = EstadoAgendamento.AGENDADO;
        this.tokenCliente = UUID.randomUUID().toString();
        this.origem = origem;
        this.criadoEm = agora;
    }

    /**
     * Associa o agendamento à conta da cliente (RF026).
     * 
     * @param contaCliente conta da cliente logada
     */
    public void vincularConta(ContaCliente contaCliente) {
        this.contaCliente = contaCliente;
    }

    /**
     * Associa o agendamento à empresa que o criou (RF021).
     * 
     * @param empresa empresa que agendou
     */
    public void vincularEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    /* ===== OPERAÇÕES ===== */
    /**
     * Cancela o agendamento e registra quem cancelou e quando.
     * O horário é liberado automaticamente, pois o banco só considera
     * agendamentos com estado AGENDADO (RF049).
     * 
     * @param ator quem está cancelando
     * @param agora momento do cancelamento
     * @throws IllegalStateException se o agendamento já estiver cancelado
     */
    public void cancelar(AtorAgendamento ator, LocalDateTime agora) {
        if (estado == EstadoAgendamento.CANCELADO) {
            throw new IllegalStateException("O agendamento já está cancelado.");
        }
        this.estado = EstadoAgendamento.CANCELADO;
        this.canceladoPor = Objects.requireNonNull(ator, "ator");
        this.canceladoEm = Objects.requireNonNull(agora, "agora");
    }

    /**
     * Move o agendamento para um novo horário e registra quem remarcou e
     * quando. A duração original do agendamento é mantida, mesmo que o
     * procedimento tenha sido alterado depois.
     * 
     * @param novoInicio novo horário de início
     * @param ator quem está remarcando
     * @param agora momento da remarcação
     * @throws IllegalStateException se o agendamento estiver cancelado
     */
    public void remarcar(LocalDateTime novoInicio, AtorAgendamento ator, LocalDateTime agora) {
        if (estado == EstadoAgendamento.CANCELADO) {
            throw new IllegalStateException("Não é possível remarcar um agendamento cancelado.");
        }
        Duration duracao = getDuracaoAgendamento();
        this.inicioAgendamento = Objects.requireNonNull(novoInicio, "novoInicio");
        this.fimAgendamento = novoInicio.plus(duracao);
        this.remarcadoPor = Objects.requireNonNull(ator, "ator");
        this.remarcadoEm = Objects.requireNonNull(agora, "agora");
    }

    /* ===== GETTERS & SETTERS ===== */
    /**
     * Método para obter o id do agendamento
     * 
     * @return id do agendamento
     */
    public Long getId() {
        return id;
    }

    /**
     * Método para obter o início do agendamento
     * 
     * @return data e hora de início
     */
    public LocalDateTime getInicioAgendamento() {
        return inicioAgendamento;
    }

    /**
     * Método para obter o fim do agendamento
     * 
     * @return data e hora de término
     */
    public LocalDateTime getFimAgendamento() {
        return fimAgendamento;
    }

    /**
     * Método para obter o nome do(a) cliente
     * 
     * @return nome do(a) cliente
     */
    public String getNomeCliente() {
        return cliente.getNomeCliente();
    }

    /**
     * Método para obter o nº de telefone do cliente
     * 
     * @return telefone do cliente
     */
    public String getTelefoneCliente() {
        return cliente.getTelefone();
    }

    /**
     * Método para obter o procedimento a ser realizado
     * 
     * @return procedimento
     */
    public Procedimento getProcedimento() {
        return procedimento;
    }

    /**
     * Método para obter o profissional responsável pelo horário
     * 
     * @return profissional dono do agendamento
     */
    public Profissional getProfissional() {
        return profissional;
    }

    /**
     * Método para obter o estado do agendamento
     * 
     * @return estado do agendamento
     */
    public EstadoAgendamento getEstado() {
        return estado;
    }

    /**
     * Método para obter o código do link único da cliente
     * 
     * @return código do link único
     */
    public String getTokenCliente() {
        return tokenCliente;
    }

    /**
     * Método para obter a conta da cliente
     * 
     * @return conta da cliente, ou nulo se agendou sem login
     */
    public ContaCliente getContaCliente() {
        return contaCliente;
    }

    /**
     * Método para obter a empresa que criou o agendamento
     * 
     * @return empresa, ou nulo se não foi criado por empresa
     */
    public Empresa getEmpresa() {
        return empresa;
    }

    /**
     * Método para obter quem criou o agendamento
     * 
     * @return origem do agendamento
     */
    public AtorAgendamento getOrigem() {
        return origem;
    }

    /**
     * Método para obter o momento em que o agendamento foi criado
     * 
     * @return data e hora da criação
     */
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    /**
     * Método para obter quem remarcou o agendamento
     * 
     * @return quem remarcou, ou nulo se nunca foi remarcado
     */
    public AtorAgendamento getRemarcadoPor() {
        return remarcadoPor;
    }

    /**
     * Método para obter o momento da última remarcação
     * 
     * @return data e hora da remarcação, ou nulo
     */
    public LocalDateTime getRemarcadoEm() {
        return remarcadoEm;
    }

    /**
     * Método para obter quem cancelou o agendamento
     * 
     * @return quem cancelou, ou nulo se não foi cancelado
     */
    public AtorAgendamento getCanceladoPor() {
        return canceladoPor;
    }

    /**
     * Método para obter o momento do cancelamento
     * 
     * @return data e hora do cancelamento, ou nulo
     */
    public LocalDateTime getCanceladoEm() {
        return canceladoEm;
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
    public Duration getDuracaoAgendamento() {
        return Duration.between(inicioAgendamento, fimAgendamento);
    }

} // Agendamento
