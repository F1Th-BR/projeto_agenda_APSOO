package com.trabalhoapsoo.sai.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.trabalhoapsoo.sai.dto.AgendamentoPublicoRequest;
import com.trabalhoapsoo.sai.dto.AgendamentoPublicoResponse;
import com.trabalhoapsoo.sai.dto.PerfilPublicoResponse;
import com.trabalhoapsoo.sai.dto.RemarcacaoRequest;
import com.trabalhoapsoo.sai.service.ServicosAgendamento;
import com.trabalhoapsoo.sai.service.ServicosAgendamento.LinkResolvido;

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
 * Endpoints PÚBLICOS (sem login) usados pela cliente (RF027, RF032–RF037).
 * 
 *  GET    /publico/{link}                       tipo do link e procedimentos
 *  GET    /publico/{link}/horarios              horários livres (procedimentoId, data)
 *  POST   /publico/{link}/agendamentos          cria o agendamento
 *  GET    /publico/agendamentos/{token}         consulta pelo link único
 *  PUT    /publico/agendamentos/{token}         remarca pelo link único
 *  DELETE /publico/agendamentos/{token}         cancela pelo link único
 * 
 * @author Thiago Feijó de Albuquerque
 */
@RestController
@RequestMapping("/publico")
public class PublicoController {
    /* ===== ATRIBUTOS ===== */
    private final ServicosAgendamento servicos;

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     * 
     * @param servicos regras de agendamento
     */
    public PublicoController(ServicosAgendamento servicos) {
        this.servicos = servicos;
    }

    /**
     * Página inicial do link (RF027, RF039): diz se o link é de profissional ou
     * de empresa e lista os procedimentos (cada um com os profissionais que o realizam).
     * 
     * @param link código do link público
     * @return perfil público
     */
    @GetMapping("/{link}")
    public PerfilPublicoResponse perfil(@PathVariable String link) {
        LinkResolvido r = servicos.resolverLink(link);
        String nome = (r.profissional() != null) ? r.profissional().getNome() : r.empresa().getNome();
        return PerfilPublicoResponse.de(r.tipo(), nome, servicos.itensDoLink(r));
    }

    /**
     * Horários livres de um dia. Informe procedimentoId (um(a) profissional) OU chave
     * (qualquer profissional disponível de uma empresa).
     * 
     * @param link código do link público
     * @param procedimentoId procedimento desejado
     * @param chave chave do item de empresa
     * @param data dia desejado (AAAA-MM-DD)
     * @return horários de início livres
     */
    @GetMapping("/{link}/horarios")
    public List<LocalDateTime> horarios(@PathVariable String link,
                                        @RequestParam(required = false) Long procedimentoId,
                                        @RequestParam(required = false) String chave,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return servicos.horariosLivres(link, procedimentoId, chave, data);
    }

    /**
     * Cria um agendamento. A resposta traz o 'token' do link único da cliente.
     * Se a cliente estiver logada (Authorization: Bearer), usa os dados da conta (RF034).
     * 
     * @param jwt token da cliente, se houver
     * @param link código do link público
     * @param requisicao dados da cliente e horário escolhido
     * @return agendamento criado
     */
    @PostMapping("/{link}/agendamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoPublicoResponse agendar(@AuthenticationPrincipal Jwt jwt, @PathVariable String link,
                                              @Valid @RequestBody AgendamentoPublicoRequest requisicao) {
        // Só um token do tipo CLIENTE vincula a conta; token de outro tipo é tratado como "sem login"
        Long contaId = (jwt != null && "CLIENTE".equals(jwt.getClaimAsString("tipo"))) ? Long.valueOf(jwt.getSubject()) : null;
        return AgendamentoPublicoResponse.de(servicos.agendarPelaCliente(link, requisicao.procedimentoId(),
            requisicao.chave(), requisicao.nomeCliente(), requisicao.telefoneCliente(), requisicao.inicio(), contaId));
    }

    /**
     * Consulta um agendamento pelo link único.
     * 
     * @param token código do link único
     * @return agendamento
     */
    @GetMapping("/agendamentos/{token}")
    public AgendamentoPublicoResponse consultar(@PathVariable String token) {
        return AgendamentoPublicoResponse.de(servicos.buscarPorToken(token));
    }

    /**
     * Remarca um agendamento pelo link único.
     * 
     * @param token código do link único
     * @param requisicao novo horário
     * @return agendamento remarcado
     */
    @PutMapping("/agendamentos/{token}")
    public AgendamentoPublicoResponse remarcar(@PathVariable String token, @Valid @RequestBody RemarcacaoRequest requisicao) {
        return AgendamentoPublicoResponse.de(servicos.remarcarPelaCliente(token, requisicao.inicio()));
    }

    /**
     * Cancela um agendamento pelo link único.
     * 
     * @param token código do link único
     * @return agendamento cancelado
     */
    @DeleteMapping("/agendamentos/{token}")
    public AgendamentoPublicoResponse cancelar(@PathVariable String token) {
        return AgendamentoPublicoResponse.de(servicos.cancelarPelaCliente(token));
    }

} // PublicoController
