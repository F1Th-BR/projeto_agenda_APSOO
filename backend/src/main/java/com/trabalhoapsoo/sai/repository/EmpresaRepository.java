package com.trabalhoapsoo.sai.repository;

import java.util.Optional;

import com.trabalhoapsoo.sai.model.Empresa;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório responsável pelo acesso à tabela 'empresa'.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    /**
     * Busca uma empresa pelo e-mail (login).
     * 
     * @param email e-mail normalizado
     * @return empresa, ou vazio
     */
    Optional<Empresa> findByEmail(String email);

    /**
     * Verifica se o e-mail já está cadastrado.
     * 
     * @param email e-mail normalizado
     * @return true se já existe
     */
    boolean existsByEmail(String email);

    /**
     * Busca uma empresa pelo código do link público.
     * 
     * @param linkPublico código do link
     * @return empresa, ou vazio
     */
    Optional<Empresa> findByLinkPublico(String linkPublico);

} // EmpresaRepository
