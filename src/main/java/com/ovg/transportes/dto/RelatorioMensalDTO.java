package com.ovg.transportes.dto;

import java.util.List;

public record RelatorioMensalDTO(
    long totalViagens,
    List<ContagemDTO> porMotorista,
    List<ContagemDTO> porTipoVeiculo,
    List<ContagemDTO> porStatus,
    List<ContagemDTO> porMes
) {
}
