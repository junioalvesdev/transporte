package com.ovg.transportes.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Controle de acesso por endpoint + por metodo (@PreAuthorize nos controllers/services).
 * Sessao HTTP classica (cookie JSESSIONID) em vez de JWT: sistema interno, consumido
 * pelo proprio frontend MPA no mesmo dominio, sem necessidade da complexidade de token.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(
        TesteAuthenticationProvider testeAuthenticationProvider,
        AdBindAuthenticationProvider adBindAuthenticationProvider
    ) {
        // teste primeiro: so intercepta login/senha "admin"/"admin" quando habilitado
        // via configuracao; qualquer outra credencial cai pro bind real no AD
        return new ProviderManager(testeAuthenticationProvider, adBindAuthenticationProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // API e formulario same-origin (mesmo dominio do frontend); protecao real contra
            // acesso indevido vem da autenticacao de sessao + @PreAuthorize por perfil abaixo.
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                // paginas HTML/CSS/JS sao publicas (nao expoem dado nenhum); a protecao
                // de verdade esta nas chamadas /api/** que elas fazem, abaixo.
                .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/favicon.ico").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated())
            .exceptionHandling(handling -> handling.authenticationEntryPoint(
                (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
            .logout(logout -> logout.logoutUrl("/api/auth/logout"));

        return http.build();
    }
}
