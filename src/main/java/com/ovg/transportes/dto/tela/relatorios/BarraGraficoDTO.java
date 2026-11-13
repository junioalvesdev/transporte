package com.ovg.transportes.dto.tela.relatorios;

/**
 * Uma barra do grafico "Viagens por mes", desenhada com a barra de progresso
 * do Bootstrap: o Java ja calcula a largura (estilo) em relacao ao maior mes.
 */
public record BarraGraficoDTO(
    String rotulo,          // "09/2026"
    long quantidade,
    String estilo           // "width: 42%"
) {
}
