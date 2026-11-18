package com.trabalhoapsoo.sai.controller;

import com.trabalhoapsoo.sai.dto.PerfilRequest;
import com.trabalhoapsoo.sai.dto.PerfilResponse;
import com.trabalhoapsoo.sai.exceptions.RecursoNaoEncontradoException;
import com.trabalhoapsoo.sai.model.Profissional;
import com.trabalhoapsoo.sai.repository.ProfissionalRepository;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Perfil do(a) profissional logado(a): dados e link público de agendamento (RF027).
 * 
 *  GET /perfil   consulta (inclui 'linkPublico')
 *  PUT /perfil   atualiza nome e telefone
 * 
 * @author Thiago Feijó de Albuquerque
 */
@RestController
@RequestMapping("/perfil")
public class PerfilController {
    /* ===== ATRIBUTOS ===== */
    private final ProfissionalRepository repository;

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     * 
     * @param repository repositório de profissionais
     */
    public PerfilController(ProfissionalRepository repository) {
        this.repository = repository;
    }

    /**
     * Consulta o perfil.
     * 
     * @param jwt token do(a) profissional logado(a)
     * @return dados do perfil
     */
    @GetMapping
    @Transactional(readOnly = true)
    public PerfilResponse consultar(@AuthenticationPrincipal Jwt jwt) {
        return PerfilResponse.de(buscar(jwt));
    }

    /**
     * Atualiza nome e telefone.
     * 
     * @param jwt token do(a) profissional logado(a)
     * @param requisicao novos dados
     * @return perfil atualizado
     */
    @PutMapping
    @Transactional
    public PerfilResponse atualizar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PerfilRequest requisicao) {
        Profissional p = buscar(jwt);
        p.atualizarPerfil(requisicao.nome().trim(),
            requisicao.telefone() == null || requisicao.telefone().isBlank() ? null : requisicao.telefone().trim());
        p.configurar(
            requisicao.antecedenciaLembreteMinutos() != null ? requisicao.antecedenciaLembreteMinutos() : p.getAntecedenciaLembreteMinutos(),
            requisicao.prazoCancelamentoHoras() != null ? requisicao.prazoCancelamentoHoras() : p.getPrazoCancelamentoHoras());
        return PerfilResponse.de(repository.save(p));
    }

    /* ===== MÉTODOS PRIVADOS ===== */
    private Profissional buscar(Jwt jwt) {
        return repository.findById(Long.valueOf(jwt.getSubject()))
            .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado."));
    }

} // PerfilController
