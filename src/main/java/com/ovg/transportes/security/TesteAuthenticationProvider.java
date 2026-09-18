package com.ovg.transportes.security;

import com.ovg.transportes.model.Usuario;
import com.ovg.transportes.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Login "Normal" (usuario/senha fixos admin/admin) pra testar o sistema sem
 * depender do AD real — util em ambiente local/dev, onde o servidor LDAP da
 * OVG nao e alcancavel. So funciona se transportes.auth-teste.habilitado=true
 * (desligado por padrao; NUNCA deve ser ligado em producao).
 *
 * Registrado ANTES do AdBindAuthenticationProvider: se login/senha nao forem
 * exatamente "admin"/"admin", devolve null (nao autentica nem rejeita), e o
 * ProviderManager passa pro proximo provider — o bind real no AD continua
 * intacto pra qualquer outro usuario.
 */
@Component
public class TesteAuthenticationProvider implements AuthenticationProvider {

    private static final String LOGIN_TESTE = "admin";
    private static final String SENHA_TESTE = "admin";
    private static final String LOGIN_AD_USUARIO_TESTE = "admin.teste";

    private final boolean habilitado;
    private final UsuarioRepository usuarioRepository;

    public TesteAuthenticationProvider(
        @Value("${transportes.auth-teste.habilitado:false}") boolean habilitado,
        UsuarioRepository usuarioRepository
    ) {
        this.habilitado = habilitado;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!habilitado) {
            return null;
        }
        String login = authentication.getName();
        String senha = String.valueOf(authentication.getCredentials());
        if (!LOGIN_TESTE.equals(login) || !SENHA_TESTE.equals(senha)) {
            return null;
        }

        Set<GrantedAuthority> authorities = usuarioRepository.buscarPorLoginAd(LOGIN_AD_USUARIO_TESTE)
            .map(this::paraAuthorities)
            .orElseGet(() -> Set.of(new SimpleGrantedAuthority("ROLE_TRANSPORTE")));

        return new UsernamePasswordAuthenticationToken(LOGIN_AD_USUARIO_TESTE, null, authorities);
    }

    private Set<GrantedAuthority> paraAuthorities(Usuario usuario) {
        return usuario.getPerfis().stream()
            .map(perfil -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + perfil.getCodigo()))
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean supports(Class<?> tipoAutenticacao) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(tipoAutenticacao);
    }
}
