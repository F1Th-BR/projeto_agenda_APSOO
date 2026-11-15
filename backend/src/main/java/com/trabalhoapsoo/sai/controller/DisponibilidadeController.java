package com.trabalhoapsoo.sai.controller;

import java.util.List;

import com.trabalhoapsoo.sai.dto.DisponibilidadeRequest;
import com.trabalhoapsoo.sai.dto.DisponibilidadeResponse;
import com.trabalhoapsoo.sai.service.ServicosDisponibilidade;

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
 * Endpoints para o(a) profissional definir em quais dias e horários
 * atende (RF005). Exigem token JWT.
 * 
 * @author Thiago Feijó de Albuquerque
 */
@RestController
@RequestMapping("/disponibilidades")
public class DisponibilidadeController {
    /* ===== ATRIBUTOS ===== */
    private final ServicosDisponibilidade servicos;

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     * 
     * @param servicos serviço de disponibilidades
     */
    public DisponibilidadeController(ServicosDisponibilidade servicos) {
        this.servicos = servicos;
    }

    /**
     * POST /disponibilidades - cadastra um intervalo de atendimento.
     * 
     * @param jwt token do(a) profissional logado(a)
     * @param requisicao dia da semana, hora de início e hora de término
     * @return intervalo criado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DisponibilidadeResponse criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DisponibilidadeRequest requisicao) {
        return DisponibilidadeResponse.de(servicos.criar(
            idDoProfissional(jwt),
            requisicao.diaSemana(),
            requisicao.horaInicio(),
            requisicao.horaFim()));
    }

    /**
     * GET /disponibilidades - lista os intervalos do(a) profissional logado(a).
     * 
     * @param jwt token do(a) profissional logado(a)
     * @return intervalos, ordenados por dia da semana e hora
     */
    @GetMapping
    public List<DisponibilidadeResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return servicos.listar(idDoProfissional(jwt)).stream()
            .map(DisponibilidadeResponse::de)
            .toList();
    }

    /**
     * DELETE /disponibilidades/{id} - remove um intervalo.
     * 
     * @param jwt token do(a) profissional logado(a)
     * @param id id da disponibilidade
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

} // DisponibilidadeController
