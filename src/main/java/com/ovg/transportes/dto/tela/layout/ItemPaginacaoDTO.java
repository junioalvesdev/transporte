package com.ovg.transportes.dto.tela.layout;

/** Um botao da paginacao: texto ("1", "Anterior"...), link e classe (active/disabled). */
public record ItemPaginacaoDTO(String texto, String url, String classe) {
}
