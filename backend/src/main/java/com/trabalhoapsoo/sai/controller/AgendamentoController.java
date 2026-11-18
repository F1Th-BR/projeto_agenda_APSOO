package com.trabalhoapsoo.sai.controller;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import com.trabalhoapsoo.sai.dto.AgendamentoManualRequest;
import com.trabalhoapsoo.sai.dto.AgendamentoResponse;
import com.trabalhoapsoo.sai.dto.RemarcacaoRequest;
import com.trabalhoapsoo.sai.service.ServicosAgendamento;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints da agenda do(a) profissional logado(a) (RF007–RF011).
 * 
 *  GET    /agendamentos?de=&ate=     agenda do período (padrão: hoje até +30 dias)
 *  POST   /agendamentos              marca atendimento manualmente
 *  PUT    /agendamentos/{id}         remarca
 *  DELETE /agendamentos/{id}         cancela
 * 
 * @author Thiago Feijó de Albuquerque
 */
@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {
    /* ===== ATRIBUTOS ===== */
    private final ServicosAgendamento servicos;
    private final Clock relogio; // para o período padrão da listagem

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     * 
     * @param servicos regras de agendamento
     * @param relogio relógio do sistema
     */
    public AgendamentoController(ServicosAgendamento servicos, Clock relogio) {
        this.servicos = servicos;
        this.relogio = relogio;
    }

    /**
     * Lista a agenda do período.
     * 
     * @param jwt token do(a) profissional logado(a)
     * @param de primeiro dia (opcional; padrão hoje)
     * @param ate último dia (opcional; padrão 30 dias depois de 'de')
     * @return agendamentos do período
     */
    @GetMapping
    public List<AgendamentoResponse> listar(@AuthenticationPrincipal Jwt jwt,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        LocalDate inicio = (de != null) ? de : LocalDate.now(relogio);
        LocalDate fim = (ate != null) ? ate : inicio.plusDays(30);
        return servicos.listar(idDoProfissional(jwt), inicio, fim).stream().map(AgendamentoResponse::de).toList();
    }

    /**
     * Marca um atendimento manualmente.
     * 
     * @param jwt token do(a) profissional logado(a)
     * @param requisicao dados do atendimento
     * @return agendamento criado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoResponse criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AgendamentoManualRequest requisicao) {
        return AgendamentoResponse.de(servicos.agendarManual(idDoProfissional(jwt), requisicao.procedimentoId(),
            requisicao.nomeCliente(), requisicao.telefoneCliente(), requisicao.inicio()));
    }

    /**
     * Remarca um agendamento.
     * 
     * @param jwt token do(a) profissional logado(a)
     * @param id id do agendamento
     * @param requisicao novo horário
     * @return agendamento remarcado
     */
    @PutMapping("/{id}")
    public AgendamentoResponse remarcar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                        @Valid @RequestBody RemarcacaoRequest requisicao) {
        return AgendamentoResponse.de(servicos.remarcarPeloProfissional(idDoProfissional(jwt), id, requisicao.inicio()));
    }

    /**
     * Cancela um agendamento (o horário volta a ficar livre).
     * 
     * @param jwt token do(a) profissional logado(a)
     * @param id id do agendamento
     * @return agendamento cancelado
     */
    @DeleteMapping("/{id}")
    public AgendamentoResponse cancelar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return AgendamentoResponse.de(servicos.cancelarPeloProfissional(idDoProfissional(jwt), id));
    }

    /* ===== MÉTODOS PRIVADOS ===== */
    // O id do(a) profissional logado(a) está no 'subject' do token (ver ServicosAuth)
    private Long idDoProfissional(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

} // AgendamentoController
