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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;


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
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/auth/**", "/publico/**", "/webhooks/**", "/error").permitAll()
                // Cada tipo de conta só acessa as próprias rotas (RNF007)
                .requestMatchers("/empresa/**").hasRole("EMPRESA")
                .requestMatchers("/cliente/**").hasRole("CLIENTE")
                .requestMatchers("/perfil/**", "/procedimentos/**", "/disponibilidades/**", "/bloqueios/**",
                                 "/agendamentos/**", "/vinculos/**").hasRole("PROFISSIONAL")
                .anyRequest().denyAll())
            // Valida o token JWT recebido no cabeçalho Authorization: Bearer
            .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(conversorDeToken())));
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

    /**
     * Converte o token em permissões: a claim 'tipo' (PROFISSIONAL, EMPRESA ou
     * CLIENTE) vira a permissão ROLE_<tipo>. Tokens sem 'tipo' (gerados antes desta
     * versão) não têm acesso — basta fazer login de novo.
     * 
     * @return conversor de token
     */
    private JwtAuthenticationConverter conversorDeToken() {
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(jwt -> {
            String tipo = jwt.getClaimAsString("tipo");
            return tipo == null ? List.<GrantedAuthority>of() : List.<GrantedAuthority>of(new SimpleGrantedAuthority("ROLE_" + tipo));
        });
        return conversor;
    }

    /**
     * Libera o acesso do front-end (outra porta/endereço) à API (CORS).
     * As origens permitidas vêm de 'app.cors.origens' (separadas por vírgula).
     * 
     * @param origens origens permitidas
     * @return configuração de CORS
     */
    @Bean
    public CorsConfigurationSource configuracaoCors(@Value("${app.cors.origens:http://localhost:3000,http://localhost:5173}") List<String> origens) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(origens);
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", c);
        return fonte;
    }
} // ConfigSeguranca
