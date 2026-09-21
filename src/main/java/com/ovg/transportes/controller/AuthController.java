package com.ovg.transportes.controller;

import com.ovg.transportes.dto.LoginRequestDTO;
import com.ovg.transportes.dto.UsuarioLogadoDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unico ponto onde a senha do usuario passa pelo backend: chega no corpo do POST,
 * e usada uma vez para o bind no AD (AdBindAuthenticationProvider) e descartada —
 * nunca gravada em sessao, banco ou log.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public UsuarioLogadoDTO login(
        @RequestBody @Valid LoginRequestDTO requisicao,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        Authentication autenticacao = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(requisicao.login(), requisicao.senha())
        );

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacao);
        SecurityContextHolder.setContext(contexto);
        securityContextRepository.saveContext(contexto, request, response);

        return new UsuarioLogadoDTO(
            autenticacao.getName(),
            autenticacao.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
        );
    }

    // usado pelo frontend pra saber, sem repetir login/senha, se quem esta
    // logado tem o perfil TRANSPORTE (menu e paginas restritas) — exige
    // sessao ja autenticada, como qualquer /api/**
    @GetMapping("/me")
    public UsuarioLogadoDTO usuarioAtual(Authentication autenticacao) {
        return new UsuarioLogadoDTO(
            autenticacao.getName(),
            autenticacao.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
        );
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
    }
}
