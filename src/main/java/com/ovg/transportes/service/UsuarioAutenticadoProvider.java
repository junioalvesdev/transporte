package com.ovg.transportes.service;

import com.ovg.transportes.common.RecursoNaoEncontradoException;
import com.ovg.transportes.model.Usuario;
import com.ovg.transportes.repository.UsuarioRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolve o Usuario correspondente a quem esta autenticado na sessao atual.
 * Centraliza esse lookup para nao espalhar SecurityContextHolder pelos services.
 */
@Component
public class UsuarioAutenticadoProvider {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticadoProvider(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario obterUsuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.buscarPorLoginAd(login)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario autenticado sem cadastro local: " + login));
    }
}
