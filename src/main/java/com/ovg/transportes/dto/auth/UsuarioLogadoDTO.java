package com.ovg.transportes.dto.auth;

import java.util.List;

public record UsuarioLogadoDTO(
    String login,
    List<String> perfis
) {
}
