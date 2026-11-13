package com.ovg.transportes.security;

import jakarta.servlet.http.HttpServletRequest;
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
 * Sessao HTTP classica (cookie JSESSIONID): as paginas sao renderizadas no
 * servidor, e a API JSON (/api/**) continua disponivel com a mesma sessao.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String PORTAL_ANTIGO = "http://10.237.2.35/";

    private static boolean ehChamadaDeApi(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

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
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
            // formularios same-origin; a protecao real contra acesso indevido vem
            // da autenticacao de sessao + @PreAuthorize por perfil
            .csrf(csrf -> csrf.disable())
            .authenticationManager(authenticationManager)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/api/auth/login").permitAll()
                // enderecos antigos .html (so redirecionam), CSS e icones sao publicos
                .requestMatchers("/*.html", "/css/**", "/favicon.ico").permitAll()
                // pagina de erro do Spring: sem isso, o 401/403 da API vira um
                // redirecionamento pro login (o /error cairia no ponto de entrada das paginas)
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated())
            // tela de login renderizada no servidor (templates/login.html): o form
            // envia login/senha pra /login, e o Spring Security autentica
            // (TesteAuthenticationProvider e bind no AD), sem JS nenhum
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("login")
                .passwordParameter("senha")
                .defaultSuccessUrl("/calendario", true)
                .failureUrl("/login?erro"))
            // /api/** responde 401/403; as paginas mandam pro login ou pro calendario
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint((request, response, authException) -> {
                    if (ehChamadaDeApi(request)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.sendRedirect("/login");
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (ehChamadaDeApi(request)) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    } else {
                        response.sendRedirect("/calendario");
                    }
                }))
            // o botao "Sair" e um form POST comum: depois de encerrar a sessao,
            // volta pro portal antigo
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessUrl(PORTAL_ANTIGO));

        return http.build();
    }
}
