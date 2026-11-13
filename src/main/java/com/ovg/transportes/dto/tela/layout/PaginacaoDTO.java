package com.ovg.transportes.dto.tela.layout;

import java.util.List;

/**
 * Paginacao pronta pra tela (templates/fragmentos/layout.html :: paginacao).
 * Sem itens quando so existe uma pagina.
 */
public record PaginacaoDTO(
    List<ItemPaginacaoDTO> itens,
    String legenda          // "Pagina 2 de 5 · 43 no total"
) {
}
