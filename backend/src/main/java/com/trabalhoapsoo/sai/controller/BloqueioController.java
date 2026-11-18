package com.trabalhoapsoo.sai.controller;

import java.util.List;

import com.trabalhoapsoo.sai.dto.BloqueioRequest;
import com.trabalhoapsoo.sai.dto.BloqueioResponse;
import com.trabalhoapsoo.sai.service.ServicosBloqueio;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints para o(a) profissional bloquear períodos da agenda (RF006).
 * Exigem token JWT.
 * 
 * @author Thiago Feijó de Albuquerque
 */
@RestController
@RequestMapping("/bloqueios")
public class BloqueioController {
    /* ===== ATRIBUTOS ===== */
    private final ServicosBloqueio servicos;

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     * 
     * @param servicos serviço de bloqueios
     */
    public BloqueioController(ServicosBloqueio servicos) {
        this.servicos = servicos;
    }

    /**
     * POST /bloqueios - cadastra um bloqueio de agenda.
     * 
     * @param jwt token do(a) profissional logado(a)
     * @param requisicao início, fim e motivo (opcional)
     * @return bloqueio criado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BloqueioResponse criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody BloqueioRequest requisicao) {
        return BloqueioResponse.de(servicos.criar(
            idDoProfissional(jwt),
            requisicao.inicio(),
            requisicao.fim(),
            requisicao.motivo(),
            Boolean.TRUE.equals(requisicao.cancelarAgendamentos())));
    }

    /**
     * GET /bloqueios - lista os bloqueios em andamento ou futuros.
     * 
     * @param jwt token do(a) profissional logado(a)
     * @return bloqueios, do mais próximo para o mais distante
     */
    @GetMapping
    public List<BloqueioResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return servicos.listar(idDoProfissional(jwt)).stream()
            .map(BloqueioResponse::de)
            .toList();
    }

    /**
     * DELETE /bloqueios/{id} - remove um bloqueio.
     * 
     * @param jwt token do(a) profissional logado(a)
     * @param id id do bloqueio
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        servicos.remover(idDoProfissional(jwt), id);
    }

    /* ===== MÉTODOS PRIVADOS ===== */
    // O id do(a) profissional logado(a) está no 'subject' do token (ver ServicosAuth)
    private Long idDoProfissional(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

} // BloqueioController
