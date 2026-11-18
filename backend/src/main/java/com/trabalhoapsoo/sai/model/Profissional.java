package com.trabalhoapsoo.sai.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

/**
 * Classe responsável por representar
 * o profissional.
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Entity 
public class Profissional {
    /* ===== ATRIBUTOS ===== */
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id; // (Para BD) identificador da linha da tabela
    
    @Column (unique = true, nullable = false)
    private String email; // E-mail do(a) profissional. Usado como login (RF001).
    
    // IMPORTANT: NÃO GUARDAR SENHA COMO TEXTO PURO!!!
    @Column (nullable = false)
    private String senhaHash; // Hash da senha (BCrypt). 

    @Column (nullable = false)
    private String nome; // Nome exibido para as clientes e nas mensagens

    @Column
    private String telefone; // Telefone/WhatsApp do(a) profissional (opcional)

    // Código aleatório que identifica a página pública de agendamento deste(a)
    // profissional (RF027). Não pode ser alterado depois de criado
    @Column (unique = true, nullable = false, updatable = false)
    private String linkPublico;

    // Com quanto tempo de antecedência o lembrete é enviado (RF012). Ainda não usado: depende do WhatsApp
    @Column (nullable = false)
    private int antecedenciaLembreteMinutos = 1440;

    // Prazo mínimo, em horas, para cliente/empresa cancelarem ou remarcarem (RF052)
    @Column (nullable = false)
    private int prazoCancelamentoHoras = 2;

    @OneToMany (mappedBy = "profissional")
    private List<Procedimento> listaProcedimentos = new ArrayList<>(); // Lista de procedimentos
                                                                        // da profissional

    /* ===== MÉTODOS ===== */
    /** Construtor vazio exigido pelo Hibernate. */
    protected Profissional(){}
    
    /**
     * Construtor da classe
     * 
     * @param email e-mail do(a) profissional (já normalizado)
     * @param senhaHash hash BCrypt da senha
     * @param nome nome do(a) profissional
     * @param telefone telefone/WhatsApp (pode ser nulo)
     */
    public Profissional(String email, String senhaHash, String nome, String telefone) {
        this.email = email;
        this.senhaHash = senhaHash;
        this.nome = nome;
        this.telefone = telefone;
        this.linkPublico = UUID.randomUUID().toString();
    }

    /* ===== GETTERS & SETTERS ===== */
    /**
     * Método get para retornar o id.
     * 
     * @return id do(a) profissional.
     */
    public Long getId() {
        return id;
    }

    /**
     * Método get para retornar o e-mail
     * do(a) profissional.
     * 
     * @return e-mail (login) do(a) profissional.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Método get para retornar a senha hash
     * 
     * @return senha hash.
     */
    public String getSenhaHash() {
        return senhaHash;
    }

    /**
     * Método para atualizar os dados de perfil do(a) profissional
     * 
     * @param nome novo nome
     * @param telefone novo telefone (pode ser nulo)
     */
    public void atualizarPerfil(String nome, String telefone) {
        this.nome = nome;
        this.telefone = telefone;
    }

    /**
     * Método para configurar o lembrete e o prazo de cancelamento (RF012, RF052)
     * 
     * @param antecedenciaLembreteMinutos minutos de antecedência do lembrete
     * @param prazoCancelamentoHoras horas mínimas para cliente/empresa cancelar ou remarcar
     */
    public void configurar(int antecedenciaLembreteMinutos, int prazoCancelamentoHoras) {
        this.antecedenciaLembreteMinutos = antecedenciaLembreteMinutos;
        this.prazoCancelamentoHoras = prazoCancelamentoHoras;
    }

    /**
     * Método get para a antecedência do lembrete
     * 
     * @return minutos de antecedência do lembrete
     */
    public int getAntecedenciaLembreteMinutos() {
        return antecedenciaLembreteMinutos;
    }

    /**
     * Método get para o prazo mínimo de cancelamento/remarcação
     * 
     * @return horas de prazo mínimo
     */
    public int getPrazoCancelamentoHoras() {
        return prazoCancelamentoHoras;
    }

    /**
     * Método get para retornar o nome do(a) profissional
     * 
     * @return nome do(a) profissional.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Método get para retornar o telefone do(a) profissional
     * 
     * @return telefone, ou nulo se não foi informado.
     */
    public String getTelefone() {
        return telefone;
    }

    /**
     * Método get para retornar o código do link público de agendamento
     * 
     * @return código do link público.
     */
    public String getLinkPublico() {
        return linkPublico;
    }

    /**
     * Retorna os procedimentos que este(a) profissional realiza
     * 
     * @return cópia não modificável dos procedimentos que este(a)
     *          profissional realiza
     */
    public List<Procedimento> getListaProcedimentos() {
        return List.copyOf(listaProcedimentos);
    }

 } // Profissional
