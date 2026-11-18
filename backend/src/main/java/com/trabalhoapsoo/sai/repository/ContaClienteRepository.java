package com.trabalhoapsoo.sai.repository;

import java.util.Optional;

import com.trabalhoapsoo.sai.model.ContaCliente;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório responsável pelo acesso à tabela 'conta_cliente'.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public interface ContaClienteRepository extends JpaRepository<ContaCliente, Long> {

    /**
     * Busca uma conta pelo e-mail (login).
     * 
     * @param email e-mail normalizado
     * @return conta, ou vazio
     */
    Optional<ContaCliente> findByEmail(String email);

    /**
     * Verifica se o e-mail já está cadastrado.
     * 
     * @param email e-mail normalizado
     * @return true se já existe
     */
    boolean existsByEmail(String email);

} // ContaClienteRepository
