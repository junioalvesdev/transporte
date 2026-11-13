package com.ovg.transportes.dto.tela.calendario;

import java.util.List;

/**
 * Um dia da grade (celula do mes ou coluna da semana).
 * No mes aparecem ate 3 eventos; o resto fica na lista "+N mais" (modal do dia).
 */
public record DiaCalendarioDTO(
    int numero,
    String classe,                      // "mes-celula fora-do-mes hoje" / "semana-cabecalho hoje"
    List<EventoDTO> eventosVisiveis,
    List<EventoDTO> todosEventos,
    String textoMais,                   // "+4 mais", ou null (esconde)
    String modalDiaId,
    String alvoModalDia,
    String tituloModalDia               // "Viagens de 22/09/2026"
) {
}
