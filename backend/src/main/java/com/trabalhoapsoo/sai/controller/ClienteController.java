package com.trabalhoapsoo.sai.controller;

import java.util.List;

import com.trabalhoapsoo.sai.dto.AgendamentoPublicoResponse;
import com.trabalhoapsoo.sai.service.ServicosAgendamento;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints da CLIENTE com conta (token do tipo CLIENTE).
 * 
 *  GET /cliente/agendamentos   próprios agendamentos (RF026)
 * 
 * Para remarcar ou cancelar, use o 'token' de cada agendamento nas rotas
 * /publico/agendamentos/{token} (RF036, RF037).
 * 
 * @author Thiago Feijó de Albuquerque
 */
@RestController
@RequestMapping("/cliente")
public class ClienteController {
    /* ===== ATRIBUTOS ===== */
    private final ServicosAgendamento servicos;

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     * 
     * @param servicos regras de agendamento
     */
    public ClienteController(ServicosAgendamento servicos) {
        this.servicos = servicos;
    }

    /**
     * Lista os agendamentos da cliente logada.
     * 
     * @param jwt token da cliente
     * @return agendamentos, do mais recente ao mais antigo
     */
    @GetMapping("/agendamentos")
    public List<AgendamentoPublicoResponse> agendamentos(@AuthenticationPrincipal Jwt jwt) {
        return servicos.listarDaConta(Long.valueOf(jwt.getSubject())).stream()
            .map(AgendamentoPublicoResponse::de).toList();
    }

} // ClienteController
