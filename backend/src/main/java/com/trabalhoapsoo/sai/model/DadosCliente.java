package com.trabalhoapsoo.sai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Classe para representar os dados de contato para
 * quem agenda.
 * 
 * TODO: implementar a classe e a classe Agendamento para
 * dividir responsabilidades e respeitar requisitos do projeto
 * 
 * NOTE: Essa classe servirá de base para a tabela cliente
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Embeddable
public class DadosCliente {
    /* ===== ATRIBUTOS ===== */
    @Column (name = "nome_cliente", nullable = false)
    private String nome_cliente;

    @Column (name = "telefone_cliente", nullable = false)
    private String telefone;

    /* ===== MÉTODOS ===== */
    /** Construtor vazio exigido pelo Hibernate  */
    protected DadosCliente() {}

    /**
     * Construtor da classe
     * 
     * @param nome_cliente nome do cliente
     * @param telefone_cliente telefone do cliente
     */
    public DadosCliente(String nome_cliente, String telefone_cliente) {
        this.nome_cliente = nome_cliente;
        this.telefone = telefone_cliente;
    }

    /* ===== GETTERS & SETTERS ===== */
    /**
     * Método parar retornar o nome do cliente
     * 
     * @return
     */
    public String getNome_cliente() {
        return nome_cliente;
    }

    /**
     * Método para retornar o telefone do cliente
     * 
     * @return
     */
    public String getTelefone() {
        return telefone;
    }




} // DadosCliente
