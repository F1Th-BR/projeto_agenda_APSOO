package com.trabalhoapsoo.sai.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;


/**
 * Classe para configuração de segurança
 * - Autenticação por token JWT (Java Web Token; sem login)
 *   e hash de senha com BCrypt (RF001 e RNF007)
 * 
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Configuration 
@EnableWebSecurity 
public class ConfigSeguranca {
    /* ===== MÉTODOS ===== */
    
    /**
     * Método para definir quais rotas são públicas 
     * e quais exigem token
     * 
     * NOTE: '/publico/**' é o link de agendamento da cliente (sem login) e
     * '/webhooks/**' é o encaixe para as respostas do WhatsApp. O webhook
     * precisará de verificação de assinatura antes de ir para produção. 
     * 
     * NOTE 2: Devido ao modo de funcionamento da API do whatsapp e chatbots
     * a ferramenta estará disponível para um acoplamento posterior, mas não
     * no momento atual (04-11-2026)
     * 
     * @param http construtor de configuração de segurança (Spring)
     * @return cadeia de filtros configurada
     * @throws Exception caso a configuração falhe
     */
    @Bean 
    public SecurityFilterChain filtroDeSeguranca(HttpSecurity http) throws Exception{
        http
            // CSRF protege sessões em cookie; com token no cabeçalho não é necessário
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/auth/**", "/publico/**", "/webhooks/**").permitAll()
                .anyRequest().authenticated())
            // Valida o token JWT recebido no cabeçalho Authorization: Bearer
            .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()));
        return http.build();
    }

    /**
     * Codificador usado para gerar e conferir o hash das senhas
     * 
     * @return codificador BCrypt
     */
    @Bean
    public PasswordEncoder codificadorDeSenha() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Chave secreta usada para assinar e validar os tokens (HS256).
     * Deve ter pelo menos 32 caracteres e vir do .env (JWT_SECRET).
     * 
     * @param segredo texto secreto configurado em 'app.jwt.secret'
     * @return chave HMAC derivada do segredo
     */
    @Bean
    public SecretKey chaveJwt(@Value("${app.jwt.secret}") String segredo) {
        return new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /**
     * Gera (assina) os tokens no login.
     * 
     * @param chave chave secreta de assinatura
     * @return codificador de JWT
     */
    @Bean
    public JwtEncoder codificadorJwt(SecretKey chave) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave));
    }

    /**
     * Valida os tokens recebidos nas requisições.
     * 
     * @param chave chave secreta de assinatura
     * @return decodificador de JWT
     */
    @Bean
    public JwtDecoder decodificadorJwt(SecretKey chave) {
        return NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
    }
} // ConfigSeguranca
