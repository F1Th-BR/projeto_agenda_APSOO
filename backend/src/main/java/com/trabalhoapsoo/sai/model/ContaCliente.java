package com.trabalhoapsoo.sai.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Classe responsável por estruturar a conta OPCIONAL de uma cliente (RF023).
 * Quem não tem conta agenda normalmente informando só nome e telefone.
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Entity
public class ContaCliente {
    /* ===== ATRIBUTOS ===== */
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id; // (Para o BD) id da conta

    @Column (nullable = false, unique = true)
    private String email; // login

    @Column (nullable = false)
    private String senhaHash;

    @Column (nullable = false)
    private String nomeCompleto;

    @Column (nullable = false)
    private String telefone;

    private String codigoRedefinicaoHash; // hash do código de redefinição (RF025)

    private LocalDateTime codigoExpiraEm; // validade do código

    /* ===== MÉTODOS ===== */
    /** Construtor vazio exigido pelo Hibernate */
    protected ContaCliente() {}

    /**
     * Construtor da classe
     * 
     * @param email e-mail (login)
     * @param senhaHash hash BCrypt da senha
     * @param nomeCompleto nome completo
     * @param telefone telefone/WhatsApp
     */
    public ContaCliente(String email, String senhaHash, String nomeCompleto, String telefone) {
        this.email = email;
        this.senhaHash = senhaHash;
        this.nomeCompleto = nomeCompleto;
        this.telefone = telefone;
    }

    /**
     * Guarda o hash do código de redefinição e sua validade (RF025).
     * 
     * @param hash hash do código
     * @param expiraEm momento em que o código deixa de valer
     */
    public void definirCodigo(String hash, LocalDateTime expiraEm) {
        this.codigoRedefinicaoHash = hash;
        this.codigoExpiraEm = expiraEm;
    }

    /**
     * Troca a senha e invalida o código usado.
     * 
     * @param novoHash hash da nova senha
     */
    public void trocarSenha(String novoHash) {
        this.senhaHash = novoHash;
        this.codigoRedefinicaoHash = null;
        this.codigoExpiraEm = null;
    }

    /* ===== GETTERS ===== */
    /** @return id da conta */
    public Long getId() { return id; }

    /** @return e-mail (login) */
    public String getEmail() { return email; }

    /** @return hash da senha */
    public String getSenhaHash() { return senhaHash; }

    /** @return nome completo */
    public String getNomeCompleto() { return nomeCompleto; }

    /** @return telefone/WhatsApp */
    public String getTelefone() { return telefone; }

    /** @return hash do código de redefinição (ou nulo) */
    public String getCodigoRedefinicaoHash() { return codigoRedefinicaoHash; }

    /** @return validade do código (ou nulo) */
    public LocalDateTime getCodigoExpiraEm() { return codigoExpiraEm; }

} // ContaCliente
