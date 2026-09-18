package com.ovg.transportes.security;

import com.ovg.transportes.model.Usuario;
import com.ovg.transportes.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Autentica o usuario fazendo um bind direto no Active Directory (mesma ideia do
 * legado em conexao/valida.php), mas SEM guardar a senha em lugar nenhum depois
 * do bind — diferente do legado, que reaproveitava a senha da sessao para SMTP.
 * O e-mail de notificacao passa a usar uma conta de servico propria (ver application.yml).
 *
 * Depois do bind confirmar a identidade, os PAPEIS (roles) vem do banco local
 * (Usuario -> Perfil), nunca de grupos do AD — mantem a regra de negocio de que
 * quem decide quem e FUNCIONARIO/TRANSPORTE/ADMINISTRADOR e o cadastro interno.
 */
@Component
public class AdBindAuthenticationProvider implements AuthenticationProvider {

    private final String ldapUrl;
    private final String dominio;
    private final UsuarioRepository usuarioRepository;

    public AdBindAuthenticationProvider(
        @Value("${transportes.ldap.url}") String ldapUrl,
        @Value("${transportes.ldap.dominio}") String dominio,
        UsuarioRepository usuarioRepository
    ) {
        this.ldapUrl = ldapUrl;
        this.dominio = dominio;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String login = authentication.getName();
        String senha = String.valueOf(authentication.getCredentials());

        confirmarBindNoActiveDirectory(login, senha);

        Set<GrantedAuthority> authorities = usuarioRepository.buscarPorLoginAd(login)
            .map(this::paraAuthorities)
            .orElseGet(() -> Set.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO")));

        // credenciais nulas no token final: depois de autenticado, a senha nao fica guardada em memoria alguma
        return new UsernamePasswordAuthenticationToken(login, null, authorities);
    }

    private void confirmarBindNoActiveDirectory(String login, String senha) {
        Hashtable<String, String> propriedades = new Hashtable<>();
        propriedades.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        propriedades.put(Context.PROVIDER_URL, ldapUrl);
        propriedades.put(Context.SECURITY_AUTHENTICATION, "simple");
        propriedades.put(Context.SECURITY_PRINCIPAL, "%s@%s".formatted(login, dominio));
        propriedades.put(Context.SECURITY_CREDENTIALS, senha);

        try {
            new InitialDirContext(propriedades).close();
        } catch (NamingException ex) {
            throw new BadCredentialsException("Usuario ou senha invalidos", ex);
        }
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
