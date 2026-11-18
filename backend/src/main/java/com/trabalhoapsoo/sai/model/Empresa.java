package com.trabalhoapsoo.sai.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Classe responsável por estruturar a conta de uma empresa (vários
 * profissionais em um único link de agendamento).
 * A empresa NÃO cria nem altera agendas ou procedimentos dos profissionais.
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Entity
public class Empresa {
    /* ===== ATRIBUTOS ===== */
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id; // (Para o BD) id da empresa

    @Column (nullable = false, unique = true)
    private String email; // login

    @Column (nullable = false)
    private String senhaHash; // hash BCrypt

    @Column (nullable = false)
    private String nome;

    private String telefone;

    @Column (nullable = false, unique = true)
    private String linkPublico; // código do link de agendamento da empresa (RF016)

    @Column (nullable = false)
    private boolean linkAtivo = true; // empresa pode desativar o link (RF016)

    /* ===== MÉTODOS ===== */
    /** Construtor vazio exigido pelo Hibernate */
    protected Empresa() {}

    /**
     * Construtor da classe
     * 
     * @param email e-mail (login)
     * @param senhaHash hash BCrypt da senha
     * @param nome nome da empresa
     * @param telefone telefone (opcional)
     */
    public Empresa(String email, String senhaHash, String nome, String telefone) {
        this.email = email;
        this.senhaHash = senhaHash;
        this.nome = nome;
        this.telefone = telefone;
        this.linkPublico = UUID.randomUUID().toString();
    }

    /**
     * Gera um novo código para o link, invalidando o anterior (RF016).
     */
    public void regenerarLink() {
        this.linkPublico = UUID.randomUUID().toString();
    }

    /**
     * Ativa ou desativa o link de agendamento (RF016).
     * 
     * @param ativo true para ativar
     */
    public void setLinkAtivo(boolean ativo) {
        this.linkAtivo = ativo;
    }

    /* ===== GETTERS ===== */
    /** @return id da empresa */
    public Long getId() { return id; }

    /** @return e-mail (login) */
    public String getEmail() { return email; }

    /** @return hash da senha */
    public String getSenhaHash() { return senhaHash; }

    /** @return nome da empresa */
    public String getNome() { return nome; }

    /** @return telefone da empresa */
    public String getTelefone() { return telefone; }

    /** @return código do link público */
    public String getLinkPublico() { return linkPublico; }

    /** @return true se o link está ativo */
    public boolean isLinkAtivo() { return linkAtivo; }

} // Empresa
