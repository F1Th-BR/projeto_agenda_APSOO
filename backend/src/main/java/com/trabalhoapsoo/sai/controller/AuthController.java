package com.trabalhoapsoo.sai.controller;

import com.trabalhoapsoo.sai.dto.CadastroRequest;
import com.trabalhoapsoo.sai.dto.LoginRequest;
import com.trabalhoapsoo.sai.dto.RedefinirSenhaRequest;
import com.trabalhoapsoo.sai.dto.SolicitarCodigoRequest;
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

    /**
     * Cadastra uma empresa (RF016).
     * 
     * @param requisicao nome, e-mail, telefone e senha
     */
    @PostMapping("/empresa/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public void cadastroEmpresa(@Valid @RequestBody CadastroRequest requisicao) {
        servicos.cadastrarEmpresa(requisicao);
    }

    /**
     * Login da empresa.
     * 
     * @param requisicao e-mail e senha
     * @return token JWT
     */
    @PostMapping("/empresa/login")
    public TokenResponse loginEmpresa(@Valid @RequestBody LoginRequest requisicao) {
        return servicos.loginEmpresa(requisicao);
    }

    /**
     * Cria a conta opcional da cliente (RF023).
     * 
     * @param requisicao nome completo, e-mail, telefone e senha
     */
    @PostMapping("/cliente/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public void cadastroCliente(@Valid @RequestBody CadastroRequest requisicao) {
        servicos.cadastrarCliente(requisicao);
    }

    /**
     * Login da cliente (RF024).
     * 
     * @param requisicao e-mail e senha
     * @return token JWT
     */
    @PostMapping("/cliente/login")
    public TokenResponse loginCliente(@Valid @RequestBody LoginRequest requisicao) {
        return servicos.loginCliente(requisicao);
    }

    /**
     * Pede o código de redefinição de senha (RF025).
     * 
     * @param requisicao e-mail e canal
     */
    @PostMapping("/cliente/senha/codigo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void solicitarCodigo(@Valid @RequestBody SolicitarCodigoRequest requisicao) {
        servicos.solicitarCodigo(requisicao);
    }

    /**
     * Define a nova senha com o código recebido (RF025).
     * 
     * @param requisicao e-mail, código e nova senha
     */
    @PostMapping("/cliente/senha/redefinir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest requisicao) {
        servicos.redefinirSenha(requisicao);
    }

} // AuthController