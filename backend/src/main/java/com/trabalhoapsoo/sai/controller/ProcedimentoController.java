package com.trabalhoapsoo.sai.controller;

import java.time.Duration;
import java.util.List;

import com.trabalhoapsoo.sai.dto.ProcedimentoRequest;
import com.trabalhoapsoo.sai.dto.ProcedimentoResponse;
import com.trabalhoapsoo.sai.service.ServicosProcedimento;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints para o(a) profissional gerenciar os próprios procedimentos
 * (RF002, RF003, RF004). Exigem token JWT.
 * 
 * @author Thiago Feijó de Albuquerque
 */
@RestController
@RequestMapping("/procedimentos")
public class ProcedimentoController {
    /* ===== ATRIBUTOS ===== */
    private final ServicosProcedimento servicos;

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     * 
     * @param servicos serviço de procedimentos
     */
    public ProcedimentoController(ServicosProcedimento servicos) {
        this.servicos = servicos;
    }

    /**
     * POST /procedimentos - cadastra um procedimento (RF002).
     * 
     * @param jwt token do(a) profissional logado(a)
     * @param requisicao nome, duração (minutos) e preço
     * @return procedimento criado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProcedimentoResponse criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProcedimentoRequest requisicao) {
        return ProcedimentoResponse.de(servicos.criar(
            idDoProfissional(jwt),
            requisicao.nome(),
            Duration.ofMinutes(requisicao.duracaoMinutos()),
            requisicao.preco()));
    }

    /**
     * GET /procedimentos - lista os procedimentos do(a) profissional logado(a).
     * 
     * @param jwt token do(a) profissional logado(a)
     * @return procedimentos do(a) profissional
     */
    @GetMapping
    public List<ProcedimentoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return servicos.listar(idDoProfissional(jwt)).stream()
            .map(ProcedimentoResponse::de)
            .toList();
    }

    /**
     * GET /procedimentos/{id} - busca um procedimento.
     * 
     * @param jwt token do(a) profissional logado(a)
     * @param id id do procedimento
     * @return procedimento encontrado
     */
    @GetMapping("/{id}")
    public ProcedimentoResponse buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return ProcedimentoResponse.de(servicos.buscar(idDoProfissional(jwt), id));
    }

    /**
     * PUT /procedimentos/{id} - edita um procedimento (RF003).
     * 
     * @param jwt token do(a) profissional logado(a)
     * @param id id do procedimento
     * @param requisicao novos nome, duração (minutos) e preço
     * @return procedimento atualizado
     */
    @PutMapping("/{id}")
    public ProcedimentoResponse atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                          @Valid @RequestBody ProcedimentoRequest requisicao) {
        return ProcedimentoResponse.de(servicos.atualizar(
            idDoProfissional(jwt),
            id,
            requisicao.nome(),
            Duration.ofMinutes(requisicao.duracaoMinutos()),
            requisicao.preco()));
    }

    /**
     * DELETE /procedimentos/{id} - remove um procedimento (RF004).
     * 
     * @param jwt token do(a) profissional logado(a)
     * @param id id do procedimento
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

} // ProcedimentoController
