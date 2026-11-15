package com.trabalhoapsoo.sai.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Define o relógio usado pelo sistema. Os horários são guardados como
 * LocalDateTime (horário local do salão), então "agora" precisa ser
 * calculado sempre no mesmo fuso, independente do fuso do servidor.
 * 
 * Nos testes, este bean pode ser trocado por um relógio fixo.
 * 
 * @author Thiago Feijó de Albuquerque
 */
@Configuration
public class ConfigRelogio {
    /* ===== ATRIBUTOS ===== */
    // Fuso em que os horários do sistema são interpretados
    public static final ZoneId FUSO_SISTEMA = ZoneId.of("America/Manaus");

    /* ===== MÉTODOS ===== */
    /**
     * Relógio do sistema, fixado no fuso de {@link #FUSO_SISTEMA}.
     * 
     * @return relógio do sistema
     */
    @Bean
    public Clock relogio() {
        return Clock.system(FUSO_SISTEMA);
    }

} // ConfigRelogio
