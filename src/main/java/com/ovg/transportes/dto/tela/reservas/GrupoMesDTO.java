package com.ovg.transportes.dto.tela.reservas;

import java.util.List;

/**
 * Um mes da tabela (bloco que abre/fecha), com as reservas dele.
 * No HTML: th:each="grupo : ${pagina.grupos}" e th:each="r : ${grupo.linhas}".
 */
public record GrupoMesDTO(
    String titulo,          // "Setembro 2026"
    String resumo,          // "12 reserva(s)"
    String collapseId,      // id do bloco que abre/fecha
    String alvoCollapse,    // "#" + collapseId, pro botao do cabecalho
    List<LinhaReservaDTO> linhas
) {
}
