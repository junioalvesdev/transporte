package com.ovg.transportes.dto.tela.layout;

/** Um item do menu do topo: texto, endereco e a classe (com "active" na pagina atual). */
public record ItemMenuDTO(String texto, String url, String classe) {
}
