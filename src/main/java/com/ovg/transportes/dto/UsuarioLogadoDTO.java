package com.ovg.transportes.dto;

import java.util.List;

public record UsuarioLogadoDTO(
    String login,
    List<String> perfis
) {
}
