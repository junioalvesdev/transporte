package com.ovg.transportes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditoriaConfig {

    /**
     * Diz ao Spring Data JPA quem e o "usuario atual" para preencher
     * criadoPor/atualizadoPor automaticamente (ver Auditavel).
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
            if (autenticacao == null || !autenticacao.isAuthenticated()) {
                return Optional.of("sistema");
            }
            return Optional.of(autenticacao.getName());
        };
    }
}
