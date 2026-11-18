package com.trabalhoapsoo.sai.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;

import com.trabalhoapsoo.sai.dto.CadastroRequest;
import com.trabalhoapsoo.sai.dto.LoginRequest;
import com.trabalhoapsoo.sai.dto.RedefinirSenhaRequest;
import com.trabalhoapsoo.sai.dto.SolicitarCodigoRequest;
import com.trabalhoapsoo.sai.dto.TokenResponse;
import com.trabalhoapsoo.sai.exceptions.ConflitoException;
import com.trabalhoapsoo.sai.exceptions.CredenciaisInvalidasException;
import com.trabalhoapsoo.sai.exceptions.RequisicaoInvalidaException;
import com.trabalhoapsoo.sai.model.ContaCliente;
import com.trabalhoapsoo.sai.model.Empresa;
import com.trabalhoapsoo.sai.model.Profissional;
import com.trabalhoapsoo.sai.notificacao.EnviadorCodigo;
import com.trabalhoapsoo.sai.repository.ContaClienteRepository;
import com.trabalhoapsoo.sai.repository.EmpresaRepository;
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
 * Classe de apoio para cadastro e login dos três tipos de conta:
 * profissional (RF001), empresa e cliente (RF023–RF025).
 * 
 * O token JWT carrega o id no 'subject' e o tipo da conta na claim 'tipo'
 * (PROFISSIONAL, EMPRESA ou CLIENTE). A segurança usa o tipo para decidir
 * quais rotas cada conta pode acessar.
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Service
public class ServicosAuth {
    /* ===== ATRIBUTOS ===== */
    public static final String TIPO_PROFISSIONAL = "PROFISSIONAL";
    public static final String TIPO_EMPRESA = "EMPRESA";
    public static final String TIPO_CLIENTE = "CLIENTE";

    // Tempo de validade do token. Depois disso, é preciso fazer login de novo
    private static final Duration VALIDADE_TOKEN = Duration.ofHours(8);
    // Validade do código de redefinição de senha (RF025)
    private static final Duration VALIDADE_CODIGO = Duration.ofMinutes(15);

    private final ProfissionalRepository repository; // acesso à tabela 'profissional'
    private final EmpresaRepository empresaRepository;
    private final ContaClienteRepository contaClienteRepository;
    private final PasswordEncoder codificadorDeSenha; // gera e confere hashes BCrypt
    private final JwtEncoder jwtEncoder; // assina os tokens
    private final EnviadorCodigo enviadorCodigo; // encaixe para e-mail/SMS
    private final Clock relogio;
    private final SecureRandom aleatorio = new SecureRandom();

    /* ===== MÉTODOS ===== */
    /**
     * Construtor da classe (injeção de dependências pelo Spring)
     * 
     * @param repository repositório de profissionais
     * @param empresaRepository repositório de empresas
     * @param contaClienteRepository repositório de contas de cliente
     * @param codificadorDeSenha codificador BCrypt
     * @param jwtEncoder codificador de tokens JWT
     * @param enviadorCodigo envio do código de redefinição
     * @param relogio relógio do sistema
     */
    public ServicosAuth(ProfissionalRepository repository, EmpresaRepository empresaRepository,
                        ContaClienteRepository contaClienteRepository, PasswordEncoder codificadorDeSenha,
                        JwtEncoder jwtEncoder, EnviadorCodigo enviadorCodigo, Clock relogio) {
        this.repository = repository;
        this.empresaRepository = empresaRepository;
        this.contaClienteRepository = contaClienteRepository;
        this.codificadorDeSenha = codificadorDeSenha;
        this.jwtEncoder = jwtEncoder;
        this.enviadorCodigo = enviadorCodigo;
        this.relogio = relogio;
    }

    /* ----- PROFISSIONAL ----- */
    /**
     * Método para cadastrar um(a) novo(a) profissional.
     * A senha é guardada apenas como hash BCrypt.
     * 
     * @param requisicao nome, e-mail, telefone e senha informados
     * @throws ConflitoException se o e-mail já estiver cadastrado
     */
    @Transactional
    public void cadastrar(CadastroRequest requisicao) {
        String email = normalizarEmail(requisicao.email());
        if (repository.existsByEmail(email)) {
            throw new ConflitoException("E-mail já cadastrado.");
        }
        repository.save(new Profissional(email, codificadorDeSenha.encode(requisicao.senha()),
            requisicao.nome().trim(), telefoneOuNulo(requisicao.telefone())));
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
        return new TokenResponse(gerarToken(profissional.getId(), profissional.getEmail(), TIPO_PROFISSIONAL));
    }

    /* ----- EMPRESA ----- */
    /**
     * Método para cadastrar uma empresa. A empresa recebe um link público próprio (RF016).
     * 
     * @param requisicao nome da empresa, e-mail, telefone e senha
     * @throws ConflitoException se o e-mail já estiver cadastrado
     */
    @Transactional
    public void cadastrarEmpresa(CadastroRequest requisicao) {
        String email = normalizarEmail(requisicao.email());
        if (empresaRepository.existsByEmail(email)) {
            throw new ConflitoException("E-mail já cadastrado.");
        }
        empresaRepository.save(new Empresa(email, codificadorDeSenha.encode(requisicao.senha()),
            requisicao.nome().trim(), telefoneOuNulo(requisicao.telefone())));
    }

    /**
     * Método para autenticar uma empresa.
     * 
     * @param requisicao e-mail e senha
     * @return token JWT do tipo EMPRESA
     * @throws CredenciaisInvalidasException se as credenciais estiverem incorretas
     */
    @Transactional(readOnly = true)
    public TokenResponse loginEmpresa(LoginRequest requisicao) {
        Empresa e = empresaRepository.findByEmail(normalizarEmail(requisicao.email()))
            .filter(x -> codificadorDeSenha.matches(requisicao.senha(), x.getSenhaHash()))
            .orElseThrow(CredenciaisInvalidasException::new);
        return new TokenResponse(gerarToken(e.getId(), e.getEmail(), TIPO_EMPRESA));
    }

    /* ----- CLIENTE ----- */
    /**
     * Método para criar a conta (opcional) de uma cliente (RF023).
     * 
     * @param requisicao nome completo, e-mail, telefone (obrigatório aqui) e senha
     * @throws RequisicaoInvalidaException se o telefone não for informado
     * @throws ConflitoException se o e-mail já estiver cadastrado
     */
    @Transactional
    public void cadastrarCliente(CadastroRequest requisicao) {
        String telefone = telefoneOuNulo(requisicao.telefone());
        if (telefone == null) {
            throw new RequisicaoInvalidaException("O telefone/WhatsApp é obrigatório.");
        }
        String email = normalizarEmail(requisicao.email());
        if (contaClienteRepository.existsByEmail(email)) {
            throw new ConflitoException("E-mail já cadastrado.");
        }
        contaClienteRepository.save(new ContaCliente(email, codificadorDeSenha.encode(requisicao.senha()),
            requisicao.nome().trim(), telefone));
    }

    /**
     * Método para autenticar uma cliente com conta (RF024).
     * 
     * @param requisicao e-mail e senha
     * @return token JWT do tipo CLIENTE
     * @throws CredenciaisInvalidasException se as credenciais estiverem incorretas
     */
    @Transactional(readOnly = true)
    public TokenResponse loginCliente(LoginRequest requisicao) {
        ContaCliente c = contaClienteRepository.findByEmail(normalizarEmail(requisicao.email()))
            .filter(x -> codificadorDeSenha.matches(requisicao.senha(), x.getSenhaHash()))
            .orElseThrow(CredenciaisInvalidasException::new);
        return new TokenResponse(gerarToken(c.getId(), c.getEmail(), TIPO_CLIENTE));
    }

    /**
     * Gera e "envia" o código de redefinição de senha (RF025). Responde sempre
     * da mesma forma, exista ou não a conta, para não revelar quais e-mails são cadastrados.
     * 
     * @param requisicao e-mail da conta e canal (EMAIL ou SMS)
     */
    @Transactional
    public void solicitarCodigo(SolicitarCodigoRequest requisicao) {
        String canal = "SMS".equalsIgnoreCase(requisicao.canal()) ? "SMS" : "EMAIL";
        contaClienteRepository.findByEmail(normalizarEmail(requisicao.email())).ifPresent(c -> {
            String codigo = String.format("%06d", aleatorio.nextInt(1_000_000));
            c.definirCodigo(codificadorDeSenha.encode(codigo), LocalDateTime.now(relogio).plus(VALIDADE_CODIGO));
            contaClienteRepository.save(c);
            enviadorCodigo.enviar(c.getEmail(), c.getTelefone(), canal, codigo);
        });
    }

    /**
     * Define a nova senha com o código recebido. Sem o código correto e dentro
     * da validade, a senha NÃO é alterada (RF025).
     * 
     * @param requisicao e-mail, código e nova senha
     * @throws RequisicaoInvalidaException se o código for inválido ou estiver expirado
     */
    @Transactional
    public void redefinirSenha(RedefinirSenhaRequest requisicao) {
        ContaCliente c = contaClienteRepository.findByEmail(normalizarEmail(requisicao.email()))
            .filter(x -> x.getCodigoRedefinicaoHash() != null && x.getCodigoExpiraEm() != null
                && x.getCodigoExpiraEm().isAfter(LocalDateTime.now(relogio))
                && codificadorDeSenha.matches(requisicao.codigo().trim(), x.getCodigoRedefinicaoHash()))
            .orElseThrow(() -> new RequisicaoInvalidaException("Código inválido ou expirado."));
        c.trocarSenha(codificadorDeSenha.encode(requisicao.novaSenha()));
        contaClienteRepository.save(c);
    }

    /* ===== MÉTODOS PRIVADOS ===== */
    // Padroniza o e-mail para que 'Ana@X.com ' e 'ana@x.com' sejam a mesma conta
    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String telefoneOuNulo(String telefone) {
        return (telefone == null || telefone.isBlank()) ? null : telefone.trim();
    }

    // Monta e assina o token. O id vai no 'subject', o tipo em 'tipo'
    private String gerarToken(Long id, String email, String tipo) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(id.toString())
            .claim("email", email)
            .claim("tipo", tipo)
            .issuedAt(agora)
            .expiresAt(agora.plus(VALIDADE_TOKEN))
            .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
    }

} // ServicosAuth
