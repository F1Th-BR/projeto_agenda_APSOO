package com.trabalhoapsoo.sai.model;

import java.util.ArrayList;
import java.util.List;

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
    private String usuario; // Nome de usuário do(a) profissional.
    
    // IMPORTANT: NÃO GUARDAR SENHA COMO TEXTO PURO!!!
    @Column (nullable = false)
    private String senhaHash; // Hash da senha (BCrypt). 

    @OneToMany (mappedBy = "profissional")
    private List<Procedimento> listaProcedimentos = new ArrayList<>(); // Lista de procedimentos
                                                                        // da profissional

    /* ===== MÉTODOS ===== */
    /** Construtor vazio exigido pelo Hibernate. */
    protected Profissional(){}
    
    /**
     * Construtor da classe
     * 
     * @param nomeUsuario
     * @param senhaHash
     */
    public Profissional(String nomeUsuario, String senhaHash) {
        this.usuario = nomeUsuario;
        this.senhaHash = senhaHash;
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
     * Método get para retornar o nome de usuário
     * do(a) profissional.
     * 
     * @return nome de usuário.
     */
    public String getUsuario() {
        return usuario;
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
     * Retorna os procedimentos que este(a) profissional realiza
     * 
     * @return cópia não modificável dos procedimentos que este(a)
     *          profissional realiza
     */
    public List<Procedimento> getListaProcedimentos() {
        return List.copyOf(listaProcedimentos);
    }

 } // Profissional