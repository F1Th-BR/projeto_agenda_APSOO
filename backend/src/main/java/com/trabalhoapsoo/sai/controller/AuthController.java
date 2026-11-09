package com.trabalhoapsoo.sai.controller;

import com.trabalhoapsoo.sai.dto.CadastroRequest;
import com.trabalhoapsoo.sai.dto.LoginRequest;
import com.trabalhoapsoo.sai.dto.TokenResponse;
import com.trabalhoapsoo.sai.service.ServicosAuth;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos de cadastro e login do(a) profissional (RF001).
 * 
 * @author Thiago Feijó de Albuquerque
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    /* ===== ATRIBUTOS ===== */
    private final ServicosAuth servicos;

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     * 
     * @param servicos serviço de autenticação
     */
    public AuthController(ServicosAuth servicos) {
        this.servicos = servicos;
    }

    /**
     * POST /auth/cadastro - cria a conta de um(a) profissional.
     * 
     * @param requisicao e-mail e senha
     */
    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public void cadastro(@Valid @RequestBody CadastroRequest requisicao) {
        servicos.cadastrar(requisicao);
    }

    /**
     * POST /auth/login - autentica e devolve o token JWT.
     * 
     * @param requisicao e-mail e senha
     * @return token JWT
     */
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest requisicao) {
        return servicos.login(requisicao);
    }

} // AuthController