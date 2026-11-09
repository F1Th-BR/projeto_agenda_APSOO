package com.trabalhoapsoo.sai.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import com.trabalhoapsoo.sai.dto.CadastroRequest;
import com.trabalhoapsoo.sai.dto.LoginRequest;
import com.trabalhoapsoo.sai.dto.TokenResponse;
import com.trabalhoapsoo.sai.exceptions.ConflitoException;
import com.trabalhoapsoo.sai.exceptions.CredenciaisInvalidasException;
import com.trabalhoapsoo.sai.model.Profissional;
import com.trabalhoapsoo.sai.repository.ProfissionalRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classe de apoio para cadastro e login de profissionais (RF001).
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Service
public class ServicosAuth {
    /* ===== ATRIBUTOS ===== */
    // Tempo de validade do token. Depois disso, é preciso fazer login de novo
    private static final Duration VALIDADE_TOKEN = Duration.ofHours(8);

    private final ProfissionalRepository repository; // acesso à tabela 'profissional'
    private final PasswordEncoder codificadorDeSenha; // gera e confere hashes BCrypt
    private final JwtEncoder jwtEncoder; // assina os tokens

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     * 
     * @param repository repositório de profissionais
     * @param codificadorDeSenha codificador BCrypt
     * @param jwtEncoder codificador de tokens JWT
     */
    public ServicosAuth(ProfissionalRepository repository, PasswordEncoder codificadorDeSenha, JwtEncoder jwtEncoder) {
        this.repository = repository;
        this.codificadorDeSenha = codificadorDeSenha;
        this.jwtEncoder = jwtEncoder;
    }

    /**
     * Método para cadastrar um(a) novo(a) profissional.
     * A senha é guardada apenas como hash BCrypt.
     * 
     * @param requisicao e-mail e senha informados
     * @throws ConflitoException se o e-mail já estiver cadastrado
     */
    @Transactional
    public void cadastrar(CadastroRequest requisicao) {
        String email = normalizarEmail(requisicao.email());
        if (repository.existsByEmail(email)) {
            throw new ConflitoException("E-mail já cadastrado.");
        }
        repository.save(new Profissional(email, codificadorDeSenha.encode(requisicao.senha())));
    }

    /**
     * Método para autenticar um(a) profissional (RF001).
     * 
     * @param requisicao e-mail e senha informados
     * @return token JWT com o id do(a) profissional no campo 'subject'
     * @throws CredenciaisInvalidasException se o e-mail ou a senha estiverem incorretos
     */
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest requisicao) {
        // Mesma exceção para e-mail inexistente e senha errada: não revela qual dos dois falhou
        Profissional profissional = repository.findByEmail(normalizarEmail(requisicao.email()))
            .filter(p -> codificadorDeSenha.matches(requisicao.senha(), p.getSenhaHash()))
            .orElseThrow(CredenciaisInvalidasException::new);
        return new TokenResponse(gerarToken(profissional));
    }

    /* ===== MÉTODOS PRIVADOS ===== */
    // Padroniza o e-mail para que 'Ana@X.com ' e 'ana@x.com' sejam a mesma conta
    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    // Monta e assina o token. O id vai no 'subject' e é usado pelos controllers
    // para saber quem está logado (ex.: dono do procedimento)
    private String gerarToken(Profissional profissional) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(profissional.getId().toString())
            .claim("email", profissional.getEmail())
            .issuedAt(agora)
            .expiresAt(agora.plus(VALIDADE_TOKEN))
            .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
    }

} // ServicosAuth
