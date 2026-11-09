package com.trabalhoapsoo.sai.repository;

import java.util.Optional;

import com.trabalhoapsoo.sai.model.Profissional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório responsável pelo acesso à tabela 'profissional'.
 * 
 * @author Thiago Feijó de Albuquerque
 */
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    /**
     * Busca um(a) profissional pelo e-mail (usado no login, RF001).
     * 
     * @param email e-mail já normalizado (minúsculas, sem espaços)
     * @return profissional encontrado, ou vazio se não existir
     */
    Optional<Profissional> findByEmail(String email);

    /**
     * Verifica se já existe um(a) profissional com o e-mail informado.
     * 
     * @param email e-mail já normalizado
     * @return true se o e-mail já estiver cadastrado
     */
    boolean existsByEmail(String email);

} // ProfissionalRepository
