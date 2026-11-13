package com.ovg.transportes.dto.tela.calendario;

/** Um status de viagem no filtro da lateral: checkbox + bolinha com a cor do status. */
public record FiltroStatusDTO(
    String valor,
    String texto,
    boolean marcado,
    String classeBolinha    // "filtro-status-bolinha cor-planejada"
) {
}
