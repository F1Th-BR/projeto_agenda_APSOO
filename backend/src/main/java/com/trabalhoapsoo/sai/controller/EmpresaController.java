package com.trabalhoapsoo.sai.controller;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import com.trabalhoapsoo.sai.dto.AgendamentoEmpresaRequest;
import com.trabalhoapsoo.sai.dto.AgendamentoResponse;
import com.trabalhoapsoo.sai.dto.ConviteRequest;
import com.trabalhoapsoo.sai.dto.EmpresaPerfilResponse;
import com.trabalhoapsoo.sai.dto.LiberacaoRequest;
import com.trabalhoapsoo.sai.dto.LinkAtivoRequest;
import com.trabalhoapsoo.sai.dto.ProfissionalProcedimentosResponse;
import com.trabalhoapsoo.sai.dto.RemarcacaoRequest;
import com.trabalhoapsoo.sai.dto.VinculoResponse;
import com.trabalhoapsoo.sai.service.ServicosAgendamento;
import com.trabalhoapsoo.sai.service.ServicosEmpresa;

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
 * Endpoints da EMPRESA logada (token do tipo EMPRESA).
 * 
 *  GET    /empresa                                perfil e link (RF016)
 *  POST   /empresa/link/regenerar                 novo código de link (RF016)
 *  PUT    /empresa/link                           ativa/desativa o link (RF016)
 *  POST   /empresa/convites                       convida profissional (RF015)
 *  GET    /empresa/vinculos                       convites pendentes e vínculos ativos
 *  GET    /empresa/procedimentos                  procedimentos por profissional (RF019)
 *  PUT    /empresa/procedimentos/{id}             libera/agrupa no link (RF017, RF018)
 *  DELETE /empresa/procedimentos/{id}             tira do link
 *  GET    /empresa/profissionais/{id}/agendamentos agenda somente leitura (RF020)
 *  GET    /empresa/agendamentos                   agendamentos criados pela empresa
 *  POST   /empresa/agendamentos                   agenda como cliente (RF021)
 *  PUT    /empresa/agendamentos/{id}              remarca (RF022)
 *  DELETE /empresa/agendamentos/{id}              cancela (RF022)
 * 
 * @author Thiago Feijó de Albuquerque
 */
@RestController
@RequestMapping("/empresa")
public class EmpresaController {
    /* ===== ATRIBUTOS ===== */
    private final ServicosEmpresa servicos;
    private final ServicosAgendamento servicosAgendamento;
    private final Clock relogio;

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     * 
     * @param servicos ações da empresa
     * @param servicosAgendamento regras de agendamento
     * @param relogio relógio do sistema
     */
    public EmpresaController(ServicosEmpresa servicos, ServicosAgendamento servicosAgendamento, Clock relogio) {
        this.servicos = servicos;
        this.servicosAgendamento = servicosAgendamento;
        this.relogio = relogio;
    }

    /**
     * Perfil da empresa.
     * 
     * @param jwt token da empresa
     * @return dados da empresa
     */
    @GetMapping
    public EmpresaPerfilResponse perfil(@AuthenticationPrincipal Jwt jwt) {
        return EmpresaPerfilResponse.de(servicos.perfil(id(jwt)));
    }

    /**
     * Gera um novo código de link.
     * 
     * @param jwt token da empresa
     * @return empresa com o novo link
     */
    @PostMapping("/link/regenerar")
    public EmpresaPerfilResponse regenerarLink(@AuthenticationPrincipal Jwt jwt) {
        return EmpresaPerfilResponse.de(servicos.regenerarLink(id(jwt)));
    }

    /**
     * Ativa ou desativa o link.
     * 
     * @param jwt token da empresa
     * @param requisicao novo estado
     * @return empresa atualizada
     */
    @PutMapping("/link")
    public EmpresaPerfilResponse definirLinkAtivo(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody LinkAtivoRequest requisicao) {
        return EmpresaPerfilResponse.de(servicos.definirLinkAtivo(id(jwt), requisicao.ativo()));
    }

    /**
     * Convida um(a) profissional.
     * 
     * @param jwt token da empresa
     * @param requisicao e-mail do(a) profissional
     * @return convite criado
     */
    @PostMapping("/convites")
    @ResponseStatus(HttpStatus.CREATED)
    public VinculoResponse convidar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ConviteRequest requisicao) {
        return VinculoResponse.de(servicos.convidar(id(jwt), requisicao.emailProfissional()));
    }

    /**
     * Lista convites pendentes e vínculos ativos.
     * 
     * @param jwt token da empresa
     * @return vínculos
     */
    @GetMapping("/vinculos")
    public List<VinculoResponse> vinculos(@AuthenticationPrincipal Jwt jwt) {
        return servicos.listarVinculos(id(jwt)).stream().map(VinculoResponse::de).toList();
    }

    /**
     * Procedimentos de cada profissional vinculado e se estão liberados no link.
     * 
     * @param jwt token da empresa
     * @return procedimentos por profissional
     */
    @GetMapping("/procedimentos")
    public List<ProfissionalProcedimentosResponse> procedimentos(@AuthenticationPrincipal Jwt jwt) {
        return servicos.procedimentosPorProfissional(id(jwt));
    }

    /**
     * Libera um procedimento no link (ou muda o nome de agrupamento).
     * 
     * @param jwt token da empresa
     * @param procedimentoId id do procedimento
     * @param requisicao nome de agrupamento (opcional)
     */
    @PutMapping("/procedimentos/{procedimentoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void liberar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long procedimentoId,
                        @Valid @RequestBody(required = false) LiberacaoRequest requisicao) {
        servicos.liberar(id(jwt), procedimentoId, requisicao == null ? null : requisicao.nomeNoLink());
    }

    /**
     * Tira um procedimento do link.
     * 
     * @param jwt token da empresa
     * @param procedimentoId id do procedimento
     */
    @DeleteMapping("/procedimentos/{procedimentoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desvincular(@AuthenticationPrincipal Jwt jwt, @PathVariable Long procedimentoId) {
        servicos.desvincular(id(jwt), procedimentoId);
    }

    /**
     * Agenda de um(a) profissional vinculado(a), somente leitura.
     * 
     * @param jwt token da empresa
     * @param profissionalId id do(a) profissional
     * @param de primeiro dia (opcional; padrão hoje)
     * @param ate último dia (opcional; padrão 30 dias depois de 'de')
     * @return agendamentos do período
     */
    @GetMapping("/profissionais/{profissionalId}/agendamentos")
    public List<AgendamentoResponse> agenda(@AuthenticationPrincipal Jwt jwt, @PathVariable Long profissionalId,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        LocalDate inicio = (de != null) ? de : LocalDate.now(relogio);
        LocalDate fim = (ate != null) ? ate : inicio.plusDays(30);
        return servicos.agendaDoProfissional(id(jwt), profissionalId, inicio, fim).stream().map(AgendamentoResponse::de).toList();
    }

    /**
     * Agendamentos criados pela empresa.
     * 
     * @param jwt token da empresa
     * @return agendamentos criados por ela
     */
    @GetMapping("/agendamentos")
    public List<AgendamentoResponse> agendamentos(@AuthenticationPrincipal Jwt jwt) {
        return servicosAgendamento.listarCriadosPelaEmpresa(id(jwt)).stream().map(AgendamentoResponse::de).toList();
    }

    /**
     * Agenda como se fosse uma cliente.
     * 
     * @param jwt token da empresa
     * @param requisicao dados do agendamento
     * @return agendamento criado
     */
    @PostMapping("/agendamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoResponse agendar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AgendamentoEmpresaRequest requisicao) {
        return AgendamentoResponse.de(servicosAgendamento.agendarPelaEmpresa(id(jwt), requisicao.procedimentoId(),
            requisicao.chave(), requisicao.nomeCliente(), requisicao.telefoneCliente(), requisicao.inicio()));
    }

    /**
     * Remarca um agendamento criado pela empresa.
     * 
     * @param jwt token da empresa
     * @param agendamentoId id do agendamento
     * @param requisicao novo horário
     * @return agendamento remarcado
     */
    @PutMapping("/agendamentos/{agendamentoId}")
    public AgendamentoResponse remarcar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long agendamentoId,
                                        @Valid @RequestBody RemarcacaoRequest requisicao) {
        return AgendamentoResponse.de(servicosAgendamento.remarcarPelaEmpresa(id(jwt), agendamentoId, requisicao.inicio()));
    }

    /**
     * Cancela um agendamento criado pela empresa.
     * 
     * @param jwt token da empresa
     * @param agendamentoId id do agendamento
     * @return agendamento cancelado
     */
    @DeleteMapping("/agendamentos/{agendamentoId}")
    public AgendamentoResponse cancelar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long agendamentoId) {
        return AgendamentoResponse.de(servicosAgendamento.cancelarPelaEmpresa(id(jwt), agendamentoId));
    }

    /* ===== MÉTODOS PRIVADOS ===== */
    // O id da empresa logada está no 'subject' do token (ver ServicosAuth)
    private Long id(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

} // EmpresaController
