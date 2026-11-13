package com.ovg.transportes.dto.tela.layout;

import java.util.List;

/**
 * Menu do topo de todas as paginas (templates/fragmentos/layout.html :: menu).
 * Montado pelo LayoutService conforme o perfil de quem esta logado.
 */
public record MenuDTO(
    String titulo,          // "Departamento de Transportes" ou "Menu"
    List<ItemMenuDTO> itens
) {
}
