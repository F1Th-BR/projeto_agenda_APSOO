package com.trabalhoapsoo.sai.controller;

import java.util.List;

import com.trabalhoapsoo.sai.dto.VinculoResponse;
import com.trabalhoapsoo.sai.service.ServicosVinculo;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints do(a) PROFISSIONAL sobre convites de empresa (RF013, RF014).
 * 
 *  GET  /vinculos/convites              convites pendentes
 *  POST /vinculos/convites/{id}/aceitar aceita
 *  POST /vinculos/convites/{id}/recusar recusa
 *  GET  /vinculos/atual                 empresa atual
 *  POST /vinculos/sair                  sai da empresa
 * 
 * @author Thiago Feijó de Albuquerque
 */
@RestController
@RequestMapping("/vinculos")
public class VinculoController {
    /* ===== ATRIBUTOS ===== */
    private final ServicosVinculo servicos;

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     * 
     * @param servicos ações de vínculo
     */
    public VinculoController(ServicosVinculo servicos) {
        this.servicos = servicos;
    }

    /**
     * Lista os convites pendentes.
     * 
     * @param jwt token do(a) profissional
     * @return convites pendentes
     */
    @GetMapping("/convites")
    public List<VinculoResponse> convites(@AuthenticationPrincipal Jwt jwt) {
        return servicos.convitesPendentes(id(jwt)).stream().map(VinculoResponse::de).toList();
    }

    /**
     * Aceita um convite.
     * 
     * @param jwt token do(a) profissional
     * @param conviteId id do convite
     * @return vínculo ativo
     */
    @PostMapping("/convites/{conviteId}/aceitar")
    public VinculoResponse aceitar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long conviteId) {
        return VinculoResponse.de(servicos.aceitar(id(jwt), conviteId));
    }

    /**
     * Recusa um convite.
     * 
     * @param jwt token do(a) profissional
     * @param conviteId id do convite
     * @return convite recusado
     */
    @PostMapping("/convites/{conviteId}/recusar")
    public VinculoResponse recusar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long conviteId) {
        return VinculoResponse.de(servicos.recusar(id(jwt), conviteId));
    }

    /**
     * Mostra a empresa à qual o(a) profissional está vinculado(a).
     * 
     * @param jwt token do(a) profissional
     * @return vínculo ativo
     */
    @GetMapping("/atual")
    public VinculoResponse atual(@AuthenticationPrincipal Jwt jwt) {
        return VinculoResponse.de(servicos.vinculoAtual(id(jwt)));
    }

    /**
     * Sai da empresa.
     * 
     * @param jwt token do(a) profissional
     * @return vínculo encerrado
     */
    @PostMapping("/sair")
    public VinculoResponse sair(@AuthenticationPrincipal Jwt jwt) {
        return VinculoResponse.de(servicos.sair(id(jwt)));
    }

    /* ===== MÉTODOS PRIVADOS ===== */
    private Long id(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

} // VinculoController
