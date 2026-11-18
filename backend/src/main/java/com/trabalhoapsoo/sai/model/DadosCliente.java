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
    private String nomeCliente;

    @Column (name = "telefone_cliente", nullable = false)
    private String telefone;

    /* ===== MÉTODOS ===== */
    /** Construtor vazio exigido pelo Hibernate  */
    protected DadosCliente() {}

    /**
     * Construtor da classe
     * 
     * @param nomeCliente nome do cliente
     * @param telefoneCliente telefone do cliente
     */
    public DadosCliente(String nomeCliente, String telefoneCliente) {
        this.nomeCliente = nomeCliente;
        this.telefone = telefoneCliente;
    }

    /* ===== GETTERS & SETTERS ===== */
    /**
     * Método parar retornar o nome do cliente
     * 
     * @return nome do cliente
     */
    public String getNomeCliente() {
        return nomeCliente;
    }

    /**
     * Método para retornar o telefone do cliente
     * 
     * @return telefone do cliente
     */
    public String getTelefone() {
        return telefone;
    }

} // DadosCliente
